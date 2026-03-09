package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IRoleRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByRoleName(RoleName roleName);
}