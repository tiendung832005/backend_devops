package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.RolePermissionStatus;
import com.ra.base_spring_boot.model.entity.RolePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionResponseDTO {

    private Long id;
    private RoleName roleName;
    private String text;
    private RolePermissionStatus status;
    private LocalDateTime createdDate;

    private Long userId;
    private String username;
    private String fullName;

    public RolePermissionResponseDTO(RolePermission rp) {
        this.id = rp.getId();
        this.roleName = rp.getRoleName();
        this.text = rp.getText();
        this.status = rp.getStatus();
        this.createdDate = rp.getCreatedDate();

        if (rp.getUser() != null) {
            this.userId = rp.getUser().getId();
            this.username = rp.getUser().getUsername();
        }
    }
}
