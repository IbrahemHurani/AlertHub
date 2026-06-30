package com.alerthub.userms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@FeignClient(name = "security-service", url = "${app.services.security-url}")
public interface SecurityClient {

    @PostMapping("/api/roles/authorize")
    ResponseEntity<AuthorizeResponse> authorizeUserPermission(@RequestBody AuthorizeRequest request);

    // DTOs مطابقة للي موجودة بالـ Security Service عشان الـ Mapping
    @Getter @Setter
    class AuthorizeRequest {
        private Long userId;
        private String permission;
    }

    @Getter @Setter
    class AuthorizeResponse {
        private Long userId;
        private String permission;
        private boolean authorized;
    }
}