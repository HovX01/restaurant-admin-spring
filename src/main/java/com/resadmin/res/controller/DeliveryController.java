package com.resadmin.res.controller;

import com.resadmin.res.dto.request.AssignDeliveryRequestDTO;
import com.resadmin.res.dto.request.ReassignDriverRequestDTO;
import com.resadmin.res.dto.request.UpdateDeliveryRequestDTO;
import com.resadmin.res.dto.request.UpdateDeliveryStatusRequestDTO;
import com.resadmin.res.dto.DeliveryDTO;
import com.resadmin.res.dto.UserDTO;
import com.resadmin.res.dto.response.ApiResponseDTO;
import com.resadmin.res.dto.response.PagedResponseDTO;
import com.resadmin.res.dto.response.StatsResponseDTO;
import com.resadmin.res.mapper.EntityMapper;
import com.resadmin.res.entity.Delivery;
import com.resadmin.res.entity.User;
import com.resadmin.res.exception.ResourceNotFoundException;
import com.resadmin.res.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('DELIVERY_STAFF')")
public class DeliveryController {
    
    @Autowired
    private DeliveryService deliveryService;
    
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getAllDeliveries() {
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDTO<PagedResponseDTO<DeliveryDTO>>> getMyDeliveries(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dispatchedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Delivery> deliveryPage = deliveryService.getDeliveriesByDriverPaginated(currentUser.getId(), pageable);
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveryPage.getContent());
        PagedResponseDTO<DeliveryDTO> pagedResponse = EntityMapper.toPagedResponseDTO(deliveryPage, deliveryDTOs);
        
        return ResponseEntity.ok(ApiResponseDTO.success("My deliveries retrieved successfully", pagedResponse));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> getDeliveryById(@PathVariable Long id) {
        Optional<Delivery> delivery = deliveryService.getDeliveryById(id);
        if (delivery.isPresent()) {
            DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery.get());
            return ResponseEntity.ok(ApiResponseDTO.success("Delivery retrieved successfully", deliveryDTO));
        } else {
            throw new ResourceNotFoundException("Delivery", "id", id);
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> getDeliveryByOrderId(@PathVariable Long orderId) {
        Optional<Delivery> delivery = deliveryService.getDeliveryByOrderId(orderId);
        if (delivery.isPresent()) {
            DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery.get());
            return ResponseEntity.ok(ApiResponseDTO.success("Delivery retrieved successfully", deliveryDTO));
        } else {
            throw new ResourceNotFoundException("Delivery", "orderId", orderId);
        }
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getDeliveriesByDriver(@PathVariable Long driverId) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getDeliveriesByStatus(@PathVariable Delivery.DeliveryStatus status) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/pending")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getPendingDeliveries() {
        List<Delivery> deliveries = deliveryService.getPendingDeliveries();
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Pending deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getActiveDeliveries() {
        List<Delivery> deliveries = deliveryService.getActiveDeliveries();
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Active deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/today")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getTodaysDeliveries() {
        List<Delivery> deliveries = deliveryService.getTodaysDeliveries();
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Today's deliveries retrieved successfully", deliveryDTOs));
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> assignDelivery(@Valid @RequestBody AssignDeliveryRequestDTO assignRequest) {
        Delivery delivery = deliveryService.assignDelivery(
            assignRequest.getOrderId(),
            assignRequest.getDriverId(),
            assignRequest.getDeliveryAddress(),
            assignRequest.getDeliveryNotes()
        );
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(delivery);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery assigned successfully", deliveryDTO));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> updateDeliveryStatus(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusRequestDTO updateRequest) {
        Delivery updatedDelivery = deliveryService.updateDeliveryStatus(id, updateRequest.getStatus());
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(updatedDelivery);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery status updated successfully", deliveryDTO));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> updateDelivery(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryRequestDTO updateRequest) {
        Delivery updatedDelivery = deliveryService.updateDelivery(
            id,
            updateRequest.getDeliveryAddress(),
            updateRequest.getDeliveryNotes()
        );
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(updatedDelivery);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery updated successfully", deliveryDTO));
    }
    
    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<DeliveryDTO>> reassignDriver(@PathVariable Long id, @Valid @RequestBody ReassignDriverRequestDTO reassignRequest) {
        Delivery updatedDelivery = deliveryService.reassignDriver(id, reassignRequest.getNewDriverId());
        DeliveryDTO deliveryDTO = EntityMapper.toDeliveryDTO(updatedDelivery);
        return ResponseEntity.ok(ApiResponseDTO.success("Driver reassigned successfully", deliveryDTO));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<String>> cancelDelivery(@PathVariable Long id) {
        deliveryService.cancelDelivery(id);
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery cancelled successfully", null));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<DeliveryDTO>>> getDeliveriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByDateRange(startDate, endDate);
        List<DeliveryDTO> deliveryDTOs = EntityMapper.toDeliveryDTOList(deliveries);
        return ResponseEntity.ok(ApiResponseDTO.success("Deliveries retrieved successfully", deliveryDTOs));
    }
    
    @GetMapping("/drivers/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<List<UserDTO>>> getAvailableDrivers() {
        List<User> drivers = deliveryService.getAvailableDrivers();
        List<UserDTO> driverDTOs = EntityMapper.toUserDTOList(drivers);
        return ResponseEntity.ok(ApiResponseDTO.success("Available drivers retrieved successfully", driverDTOs));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponseDTO<StatsResponseDTO>> getDeliveryStats() {
        StatsResponseDTO stats = StatsResponseDTO.deliveryStats(
            deliveryService.getCompletedDeliveryCount(),
            deliveryService.getActiveDeliveryCount()
        );
        return ResponseEntity.ok(ApiResponseDTO.success("Delivery statistics retrieved successfully", stats));
    }
    
}