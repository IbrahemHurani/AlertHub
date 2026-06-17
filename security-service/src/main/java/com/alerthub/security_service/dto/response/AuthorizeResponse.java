package com.alerthub.security_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeResponse {
    private Long userId;
    private String permission;
    private boolean authorized; //if true, user has the permission; if false, user does not have the permission
}