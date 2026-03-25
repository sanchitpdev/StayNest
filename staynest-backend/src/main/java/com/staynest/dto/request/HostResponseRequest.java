package com.staynest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostResponseRequest {

    @NotBlank(message = "Response cannot be empty")
    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String response;
}