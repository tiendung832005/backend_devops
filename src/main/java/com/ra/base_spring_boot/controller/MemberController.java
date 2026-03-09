package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.MemberRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberRoleRequestDTO;
import com.ra.base_spring_boot.dto.req.MemberUpdateDTO;
import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.services.IMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final IMemberService memberService;

    /**
     * @param keyword Từ khóa tìm kiếm (fullName, username, email)
     * @param status Trạng thái (true/false)
     * @param roleId ID của role để lọc
     * @param page Số trang (mặc định 0)
     * @param size Kích thước trang (mặc định 10)
     * @param sortBy Trường để sắp xếp (mặc định id)
     * @param sortDir Hướng sắp xếp (ASC/DESC, mặc định ASC)
     * @apiNote Lấy danh sách thành viên với tìm kiếm, lọc và phân loại
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<Page<UserResponseDTO>>> getAllMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponseDTO> members = memberService.findAll(keyword, status, roleId, pageable);
        
        return ResponseEntity.ok(
                ResponseWrapper.<Page<UserResponseDTO>>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(members)
                        .build()
        );
    }

    /**
     * @param id ID của thành viên
     * @apiNote Lấy thông tin chi tiết thành viên theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> getMemberById(@PathVariable Long id) {
        UserResponseDTO member = memberService.findById(id);
        return ResponseEntity.ok(
                ResponseWrapper.<UserResponseDTO>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(member)
                        .build()
        );
    }

    /**
     * @param memberRequestDTO Thông tin thành viên mới
     * @apiNote Tạo thành viên mới
     */
    @PostMapping
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> createMember(
            @Valid @RequestBody MemberRequestDTO memberRequestDTO) {
        UserResponseDTO member = memberService.create(memberRequestDTO);
        return ResponseEntity.created(URI.create("/api/v1/admin/members/" + member.getId()))
                .body(
                        ResponseWrapper.<UserResponseDTO>builder()
                                .status(HttpStatus.CREATED)
                                .code(201)
                                .data(member)
                                .build()
                );
    }

    /**
     * @param id ID của thành viên
     * @param memberUpdateDTO Thông tin cập nhật
     * @apiNote Cập nhật thông tin thành viên
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateDTO memberUpdateDTO) {
        UserResponseDTO member = memberService.update(id, memberUpdateDTO);
        return ResponseEntity.ok(
                ResponseWrapper.<UserResponseDTO>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(member)
                        .build()
        );
    }

    /**
     * @param id ID của thành viên
     * @apiNote Xóa thành viên
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<String>> deleteMember(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok(
                ResponseWrapper.<String>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Member deleted successfully")
                        .build()
        );
    }

    /**
     * @param id ID của thành viên
     * @param memberRoleRequestDTO Danh sách role IDs
     * @apiNote Phân quyền cho thành viên
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> updateMemberRoles(
            @PathVariable Long id,
            @Valid @RequestBody MemberRoleRequestDTO memberRoleRequestDTO) {
        UserResponseDTO member = memberService.updateRoles(id, memberRoleRequestDTO);
        return ResponseEntity.ok(
                ResponseWrapper.<UserResponseDTO>builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(member)
                        .build()
        );
    }
}

