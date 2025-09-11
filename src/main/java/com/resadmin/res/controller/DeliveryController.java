package com.resadmin.res.controller;

import com.resadmin.res.dto.request.AssignDeliveryRequestDTO;
import com.resadmin.res.dto.request.ReassignDriverRequestDTO;
import com.resadmin.res.dto.request.UpdateDeliveryRequestDTO;
import com.resadmin.res.dto.request.UpdateDeliveryStatusRequestDTO;
import com.resadmin.res.entity.Delivery;
import com.resadmin.res.entity.User;
import com.resadmin.res.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryById(@PathVariable Long id) {
        Optional<Delivery> delivery = deliveryService.getDeliveryById(id);
        if (delivery.isPresent()) {
            return ResponseEntity.ok(delivery.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Delivery not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDeliveryByOrderId(@PathVariable Long orderId) {
        Optional<Delivery> delivery = deliveryService.getDeliveryByOrderId(orderId);
        if (delivery.isPresent()) {
            return ResponseEntity.ok(delivery.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Delivery not found for order id: " + orderId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Delivery>> getDeliveriesByDriver(@PathVariable Long driverId) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Delivery>> getDeliveriesByStatus(@PathVariable Delivery.DeliveryStatus status) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Delivery>> getPendingDeliveries() {
        List<Delivery> deliveries = deliveryService.getPendingDeliveries();
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Delivery>> getActiveDeliveries() {
        List<Delivery> deliveries = deliveryService.getActiveDeliveries();
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/today")
    public ResponseEntity<List<Delivery>> getTodaysDeliveries() {
        List<Delivery> deliveries = deliveryService.getTodaysDeliveries();
        return ResponseEntity.ok(deliveries);
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> assignDelivery(@Valid @RequestBody AssignDeliveryRequestDTO assignRequest) {
        try {
            Delivery delivery = deliveryService.assignDelivery(
                assignRequest.getOrderId(),
                assignRequest.getDriverId(),
                assignRequest.getDeliveryAddress(),
                assignRequest.getDeliveryNotes()
            );
            return ResponseEntity.ok(delivery);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusRequestDTO updateRequest) {
        try {
            Delivery updatedDelivery = deliveryService.updateDeliveryStatus(id, updateRequest.getStatus());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Delivery status updated successfully");
            response.put("delivery", updatedDelivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> updateDelivery(@PathVariable Long id, @Valid @RequestBody UpdateDeliveryRequestDTO updateRequest) {
        try {
            Delivery updatedDelivery = deliveryService.updateDelivery(
                id,
                updateRequest.getDeliveryAddress(),
                updateRequest.getDeliveryNotes()
            );
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PatchMapping("/{id}/reassign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> reassignDriver(@PathVariable Long id, @Valid @RequestBody ReassignDriverRequestDTO reassignRequest) {
        try {
            Delivery updatedDelivery = deliveryService.reassignDriver(id, reassignRequest.getNewDriverId());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Driver reassigned successfully");
            response.put("delivery", updatedDelivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id) {
        try {
            deliveryService.cancelDelivery(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Delivery cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<Delivery>> getDeliveriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByDateRange(startDate, endDate);
        return ResponseEntity.ok(deliveries);
    }
    
    @GetMapping("/drivers/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<User>> getAvailableDrivers() {
        List<User> drivers = deliveryService.getAvailableDrivers();
        return ResponseEntity.ok(drivers);
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getDeliveryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedCount", deliveryService.getCompletedDeliveryCount());
        stats.put("activeCount", deliveryService.getActiveDeliveryCount());
        return ResponseEntity.ok(stats);
    }
    
}