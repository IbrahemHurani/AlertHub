package com.alerthub.security_service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokeRoleRequest {
    private Long userId;  // User ID from which the role is being revoked
    private Long roleId;  // Role ID that is being revoked from the user for example createAction
}