package com.resadmin.res.controller;

import com.resadmin.res.dto.UserDTO;
import com.resadmin.res.dto.request.UpdateRoleRequestDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.dto.response.PagedResponseDTO;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.entity.User;
import com.resadmin.res.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.resadmin.res.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@Tag(name = "Users", description = "User management operations")
public class UserController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of users with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<UserDTO>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by username (contains)") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by role") @RequestParam(required = false) User.Role role,
            @Parameter(description = "Filter by enabled status") @RequestParam(required = false) Boolean enabled) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> userPage = authService.getAllUsersWithFilters(pageable, username, role, enabled);
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(EntityMapper::toUserDTO)
                .collect(java.util.stream.Collectors.toList());
        
        PagedResponseDTO<UserDTO> pagedResponse = EntityMapper.toPagedResponseDTO(userPage, userDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("Users retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserById(@PathVariable Long id) {
        Optional<User> user = authService.getUserById(id);
        if (user.isPresent()) {
            UserDTO userDTO = EntityMapper.toUserDTO(user.get());
            return ResponseEntity.ok(ApiResponseDTO.success("User retrieved successfully", userDTO));
        } else {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponseDTO<UserDTO>> getUserByUsername(@PathVariable String username) {
        Optional<User> user = authService.getUserByUsername(username);
        if (user.isPresent()) {
            UserDTO userDTO = EntityMapper.toUserDTO(user.get());
            return ResponseEntity.ok(ApiResponseDTO.success("User retrieved successfully", userDTO));
        } else {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getUsersByRole(@PathVariable User.Role role) {
        List<User> users = authService.getUsersByRole(role);
        List<UserDTO> userDTOs = EntityMapper.toUserDTOList(users);
        return ResponseEntity.ok(ApiResponseDTO.success("Users retrieved successfully", userDTOs));
    }
    
    @GetMapping("/enabled/{enabled}")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getUsersByEnabledStatus(@PathVariable Boolean enabled) {
        List<User> users = authService.getUsersByEnabledStatus(enabled);
        List<UserDTO> userDTOs = EntityMapper.toUserDTOList(users);
        return ResponseEntity.ok(ApiResponseDTO.success("Users retrieved successfully", userDTOs));
    }
    
    @GetMapping("/delivery-staff/available")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAvailableDeliveryStaff() {
        List<User> users = authService.getAvailableDeliveryStaff();
        List<UserDTO> userDTOs = EntityMapper.toUserDTOList(users);
        return ResponseEntity.ok(ApiResponseDTO.success("Available delivery staff retrieved successfully", userDTOs));
    }
    
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<UserDTO>> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequestDTO updateRoleRequest) {
        User updatedUser = authService.updateUserRole(id, updateRoleRequest.getRole());
        UserDTO userDTO = EntityMapper.toUserDTO(updatedUser);
        return ResponseEntity.ok(ApiResponseDTO.success("User role updated successfully", userDTO));
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponseDTO<UserDTO>> toggleUserStatus(@PathVariable Long id) {
        User updatedUser = authService.toggleUserStatus(id);
        UserDTO userDTO = EntityMapper.toUserDTO(updatedUser);
        return ResponseEntity.ok(ApiResponseDTO.success("User status updated successfully", userDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok(ApiResponseDTO.success("User deleted successfully", null));
    }
    
}