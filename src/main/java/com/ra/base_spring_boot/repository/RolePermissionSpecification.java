package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.RolePermissionStatus;
import com.ra.base_spring_boot.model.entity.RolePermission;
import com.ra.base_spring_boot.model.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class RolePermissionSpecification {

    public static Specification<RolePermission> filter(
            String keyword,
            String status
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            root.join("user", JoinType.LEFT);
            query.distinct(true);

            // ===== KEYWORD =====
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(
                        cb.like(cb.lower(root.get("text")), like)
                );
            }

            // ===== STATUS =====
            if (status != null && !status.isBlank()
                    && !"ALL".equalsIgnoreCase(status)) {
                predicates.add(
                        cb.equal(
                                root.get("status"),
                                RolePermissionStatus.valueOf(status)
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
