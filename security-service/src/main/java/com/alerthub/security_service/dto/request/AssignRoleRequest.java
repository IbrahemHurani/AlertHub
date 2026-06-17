package com.alerthub.security_service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleRequest {
    private Long userId;// User ID to which the role is being assigned  
    private Long roleId;  // Role ID that is being assigned to the user for example createAction
}