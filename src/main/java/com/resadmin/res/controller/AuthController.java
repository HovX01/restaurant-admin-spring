package com.resadmin.res.controller;

import com.resadmin.res.dto.request.ChangePasswordRequestDTO;
import com.resadmin.res.dto.request.LoginRequestDTO;
import com.resadmin.res.dto.request.RegisterRequestDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.entity.User;
import com.resadmin.res.dto.UserDTO;

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
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Map<String, Object> loginResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", (String) loginResponse.get("token"));
            responseData.put("user", (UserDTO) loginResponse.get("user"));
            
            return ResponseEntity.ok(ApiResponseDTO.success("Login successful", responseData));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error("Authentication failed", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(e.getMessage(), "An unexpected error occurred"));
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
    public ResponseEntity<ApiResponseDTO<String>> register(
            @Parameter(description = "Registration details", required = true)
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            User user = authService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getFullName(),
                registerRequest.getRole()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("User registered successfully", "User ID: " + user.getId() + ", Username: " + user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error(e.getMessage(), "Registration failed"));
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
    public ResponseEntity<ApiResponseDTO<String>> changePassword(
            @Parameter(description = "Password change request", required = true)
            @Valid @RequestBody ChangePasswordRequestDTO changePasswordRequest) {
        try {
            authService.changePassword(
                changePasswordRequest.getUsername(),
                changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword()
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("Password changed successfully", "Password changed successfully"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error("Current password verification failed", "Invalid current password"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponseDTO.error(e.getMessage(), "Password change failed"));
        }
    }
    
    @GetMapping("/info")
    @Operation(summary = "Get current user info", description = "Get the current authenticated user's complete information")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponseDTO<UserDTO>> getCurrentUserInfo(Authentication authentication) {
        try {
            String username = authentication.getName();
            UserDTO userInfo = authService.getCurrentUserInfo(username);
            return ResponseEntity.ok(ApiResponseDTO.success("User information retrieved successfully", userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(e.getMessage(), "User not found"));
        }
    }

}