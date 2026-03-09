package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.TypeNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSummaryDTO {
    private TypeNotification type;
    private Long total;
}
