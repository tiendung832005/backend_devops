package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.dto.resp.UserResponseDTO;
import com.ra.base_spring_boot.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:roleId IS NULL OR EXISTS (SELECT 1 FROM u.roles r WHERE r.id = :roleId))")
    Page<User> searchMembers(@Param("keyword") String keyword, 
                             @Param("status") Boolean status, 
                             @Param("roleId") Long roleId, 
                             Pageable pageable);

    User findByProviderAndProviderId(String provider, String providerId);

    Optional<User> findUserById(Long id);
}
