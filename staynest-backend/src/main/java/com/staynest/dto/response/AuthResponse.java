package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long expiresIn;//Token expiration time

    public AuthResponse(String userId, String email, String firstName, String lastName, String role, Long expiresIn, String token) {
        this.userId = userId;
        this.type = "Bearer";
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.expiresIn = expiresIn;
        this.token = token;
    }
}
