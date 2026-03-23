package com.staynest.controller;

import com.staynest.dto.request.LoginRequest;
import com.staynest.dto.request.RegisterRequest;
import com.staynest.dto.response.AuthResponse;
import com.staynest.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User authentication and registration")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /*
     Register a new user.
     POST /api/auth/register
     */

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest
            ){
        AuthResponse response = authService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid @RequestBody LoginRequest loginRequest
            ){
        AuthResponse response = authService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public  ResponseEntity<String> testAuth(){
        return ResponseEntity.ok("Authentication is working! You are authenticated.");
    }
}
