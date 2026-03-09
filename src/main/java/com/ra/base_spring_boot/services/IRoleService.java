package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.entity.Roles;

public interface IRoleService {
    Roles findByRoleName(RoleName roleName);
}
