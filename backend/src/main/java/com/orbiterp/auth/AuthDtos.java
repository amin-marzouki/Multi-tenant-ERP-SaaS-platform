package com.orbiterp.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String tenantCode,
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record SignupRequest(
            @NotBlank
            @Pattern(regexp = "^[a-z0-9](?:[a-z0-9-]{0,48}[a-z0-9])?$",
                    message = "use 1 to 50 lowercase letters, digits or dashes (no dash at the start or end)")
            String tenantCode,
            @NotBlank @Size(max = 255) String companyName,
            @NotBlank @Size(max = 150) String fullName,
            @NotBlank @Size(min = 3, max = 100) String username,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record UserInfo(
            Long id,
            String username,
            String fullName,
            String email,
            String tenantCode,
            String tenantName,
            Set<String> roles) {
    }

    public record LoginResponse(String token, Instant expiresAt, UserInfo user) {
    }

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 100) String username,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 150) String fullName,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotEmpty Set<@NotBlank String> roles) {
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            String fullName,
            boolean enabled,
            Set<String> roles,
            Instant createdAt) {
    }
}
