package com.resadmin.res.controller;

import com.resadmin.res.entity.User;
import com.resadmin.res.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> loginResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", loginResponse.get("token"));
            response.put("user", loginResponse.get("user"));
            response.put("message", "Login successful");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            error.put("message", "Authentication failed");
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            error.put("message", e.getMessage());
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Registration failed",
                content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getFullName(),
                registerRequest.getRole()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("success", true);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password with current password verification")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Current password verification failed",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Password change failed",
                content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> changePassword(
            @Parameter(description = "Password change request", required = true)
            @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            authService.changePassword(
                changePasswordRequest.getUsername(),
                changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid current password");
            error.put("message", "Current password verification failed");
            error.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Password change failed");
            error.put("message", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Request DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
        private User.Role role;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public User.Role getRole() { return role; }
        public void setRole(User.Role role) { this.role = role; }
    }
    
    public static class ChangePasswordRequest {
        private String username;
        private String currentPassword;
        private String newPassword;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}