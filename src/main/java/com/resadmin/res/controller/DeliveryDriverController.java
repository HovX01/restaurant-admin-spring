package com.resadmin.res.controller;

import com.resadmin.res.dto.UserDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.entity.User;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-drivers")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DELIVERY_STAFF')")
@Tag(name = "Delivery Drivers", description = "Delivery driver management operations")
public class DeliveryDriverController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    @Operation(summary = "Get all delivery drivers", description = "Retrieve a list of all users with DELIVERY_STAFF role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Delivery drivers retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAllDeliveryDrivers() {
        List<User> drivers = authService.getUsersByRole(User.Role.DELIVERY_STAFF);
        List<UserDTO> driverDTOs = EntityMapper.toUserDTOList(drivers);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery drivers retrieved successfully", driverDTOs));
    }
    
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get available delivery drivers", description = "Retrieve a list of available delivery drivers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available delivery drivers retrieved successfully",
                content = @Content(schema = @Schema(implementation = ApiResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAvailableDeliveryDrivers() {
        List<User> drivers = authService.getAvailableDeliveryStaff();
        List<UserDTO> driverDTOs = EntityMapper.toUserDTOList(drivers);
        return ResponseEntity.ok(ApiResponseDTO.success("Available delivery drivers retrieved successfully", driverDTOs));
    }
}
