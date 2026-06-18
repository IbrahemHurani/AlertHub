package com.alerthub.security_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeRequest {
    
    private long userId;// User ID for which the authorization is being requested

    @NotBlank
    private String permission;// The specific permission or action the user is requesting access to for example createAction ,triggerScan
}
