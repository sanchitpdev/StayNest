package com.staynest.dto.request;

import com.staynest.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2 ,max = 50, message = "First name should be between 2 to 50")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2 ,max = 50, message = "Last name should be between 2 to 50")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8,message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain one digit,one lowercase,one uppercase, and one special character"
    )
    private String password;

    @Pattern(regexp = "^[0-9]{10,15}$",message = "Phone number must be 10-15 digits")
    private String phoneNumber;

    // default guest role is Guest
    private UserRole role = UserRole.GUEST;
}
