package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.StatusUser;
import com.ra.base_spring_boot.model.entity.Roles;
import com.ra.base_spring_boot.model.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class UserSpecification {

    public static Specification<User> filter(
            String keyword,
            StatusUser status,
            String role
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ❗ LUÔN loại bỏ user DELETED
            predicates.add(
                    cb.notEqual(root.get("status"), StatusUser.DELETED)
            );

            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("fullName")), likeKeyword),
                                cb.like(cb.lower(root.get("username")), likeKeyword),
                                cb.like(cb.lower(root.get("email")), likeKeyword)
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (role != null && !role.isBlank()) {
                Join<User, Roles> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(
                        cb.equal(roleJoin.get("roleName"), RoleName.valueOf(role))
                );
            }
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<User> subRoot = subquery.from(User.class);
            Join<User, Roles> subRoleJoin = subRoot.join("roles", JoinType.INNER);

            subquery.select(subRoot.get("id"))
                    .where(
                            cb.and(
                                    cb.equal(subRoot.get("id"), root.get("id")),
                                    cb.equal(subRoleJoin.get("roleName"), RoleName.ROLE_ADMIN)
                            )
                    );

            predicates.add(cb.not(cb.exists(subquery)));

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


