package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.UserUpdateRequest;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.constants.ActivityType;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import com.ra.base_spring_boot.repository.IRoleRepository;
import com.ra.base_spring_boot.repository.IUserRepository;
import com.ra.base_spring_boot.repository.UserSpecification;
import com.ra.base_spring_boot.services.IActivityLogService;
import com.ra.base_spring_boot.services.IUserService;
import com.ra.base_spring_boot.utils.ExcelGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IActivityLogService activityLogService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> importUsers(MultipartFile file) throws Exception {

        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        List<User> result = new ArrayList<>();

        boolean isFirstRow = true;

        for (Row row : sheet) {

            if (isFirstRow) {
                isFirstRow = false;
                continue;
            }

            String fullName = getString(row.getCell(0));
            String username = getString(row.getCell(1));
            String email = getString(row.getCell(2));
            String password = getString(row.getCell(3));
            String avatarUrl = getString(row.getCell(4));
            String bio = getString(row.getCell(5));
            String rolesString = getString(row.getCell(6));
            String statusStr = getString(row.getCell(7));

            StatusUser status;
            try {
                status = StatusUser.valueOf(statusStr.trim().toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Sai status trong Excel: " + statusStr);
            }

            // Convert rolesString -> Set<Roles>
            Set<Roles> roles = parseRoles(rolesString);

            User user = User.builder()
                    .fullName(fullName)
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .avatarUrl(avatarUrl)
                    .bio(bio)
                    .roles(roles)
                    .status(status)
                    .build();

            result.add(user);
        }

        userRepository.saveAll(result);

        return result;
    }

    private Set<Roles> parseRoles(String rolesString) {
        Set<Roles> roles = new HashSet<>();

        if (rolesString == null || rolesString.isEmpty()) return roles;

        String[] arr = rolesString.split(",");

        for (String r : arr) {
            String role = r.trim().toUpperCase();

            RoleName roleName = RoleName.valueOf(role);

            Roles roleEntity = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            roles.add(roleEntity);
        }

        return roles;
    }

    private String getString(Cell cell) {
        if (cell == null) return null;
        return cell.getCellType() == CellType.STRING ?
                cell.getStringCellValue() :
                String.valueOf((int) cell.getNumericCellValue());
    }

    private Boolean getBoolean(Cell cell) {
        if (cell == null) return false;
        if (cell.getCellType() == CellType.BOOLEAN) return cell.getBooleanCellValue();
        return Boolean.parseBoolean(cell.getStringCellValue());
    }

    @Override
    public ByteArrayInputStream exportUsersExcel() {
        return ExcelGenerator.usersToExcel(getAllUsers());
    }
    
    @Override
    public UserResponseDTO getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        return new UserResponseDTO(user);
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new HttpNotFound("User not found with id: " + id));
        return toResponse(user);
    }

    @Override
    public UserResponseDTO updateCurrentUserProfile(String username, String fullName, String email, String avatarUrl, String bio) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("User not found with username: " + username));
        
        // Store old values for logging
        String oldFullName = user.getFullName();
        String oldEmail = user.getEmail();
        String oldAvatarUrl = user.getAvatarUrl();
        String oldBio = user.getBio();
        
        StringBuilder changes = new StringBuilder();
        if (fullName != null && !fullName.trim().isEmpty() && !fullName.equals(oldFullName)) {
            user.setFullName(fullName);
            changes.append(String.format("FullName: %s -> %s; ", oldFullName, fullName));
        }
        if (email != null && !email.trim().isEmpty() && !email.equals(oldEmail)) {
            // Check if email is already taken by another user
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new RuntimeException("Email already exists");
                }
            });
            user.setEmail(email);
            changes.append(String.format("Email: %s -> %s; ", oldEmail, email));
        }
        if (avatarUrl != null && !avatarUrl.equals(oldAvatarUrl)) {
            user.setAvatarUrl(avatarUrl);
            changes.append("Avatar updated; ");
        }
        if (bio != null && !bio.equals(oldBio)) {
            user.setBio(bio);
            changes.append("Bio updated; ");
        }
        
        User updatedUser = userRepository.save(user);
        
        // Log profile update activity
        if (changes.length() > 0) {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                activityLogService.logActivity(
                        updatedUser,
                        ActivityType.UPDATE_PROFILE,
                        "Profile updated: " + changes.toString().trim(),
                        request
                );
            }
        }
        
        return new UserResponseDTO(updatedUser);
    }

    @Override
    public Page<UserResponseDTO> getUsers(String keyword, StatusUser status, String role, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec =
                UserSpecification.filter(keyword, status, role);

        return userRepository.findAll(spec, pageable)
                .map(UserResponseDTO::new);
    }

    @Override
    public void blockUser(Long id) {
        User user =  userRepository.findUserById(id).orElseThrow(() -> new HttpNotFound("User not found with id: " + id));
        if (user.getStatus() == StatusUser.DELETED){
            return;
        }
        if(user.getStatus() == StatusUser.ACTIVE) {
            user.setStatus(StatusUser.INACTIVE);
        }else  {
            user.setStatus(StatusUser.ACTIVE);
        }
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user =  userRepository.findUserById(id).orElseThrow(() -> new HttpNotFound("User not found with id: " + id));
        user.setStatus(StatusUser.DELETED);
        userRepository.save(user);
    }

    @Override
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new HttpNotFound("User not found with id: " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new RuntimeException("Username already exists");
                        }
                    });
            user.setUsername(request.getUsername());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Roles> newRoles = request.getRoles().stream()
                    .map(r -> RoleName.valueOf(r))
                    .map(roleName ->
                            roleRepository.findByRoleName(roleName)
                                    .orElseThrow(() ->
                                            new RuntimeException("Role not found: " + roleName))
                    )
                    .collect(Collectors.toSet());

            user.setRoles(newRoles);
        }

        userRepository.save(user);
    }


    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public UserResponseDTO toResponse(User user) {
        if (user == null) return null;
        return new UserResponseDTO(user);
    }

}
