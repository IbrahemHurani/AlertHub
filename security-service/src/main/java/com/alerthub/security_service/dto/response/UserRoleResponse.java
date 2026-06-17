package com.alerthub.security_service.dto.response;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleResponse {
    private Long userId;
    //private String username;
    //private String email;
    private List<String> roles;

}
