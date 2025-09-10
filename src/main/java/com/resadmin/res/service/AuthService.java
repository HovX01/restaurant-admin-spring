package com.resadmin.res.service;

import com.resadmin.res.entity.User;
import com.resadmin.res.repository.UserRepository;
import com.resadmin.res.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    public Map<String, Object> login(String username, String password) throws AuthenticationException {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        User user = (User) authentication.getPrincipal();
        
        // Generate JWT token with user role
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        
        String token = jwtUtil.generateToken(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", getUserInfo(user));
        
        return response;
    }
    
    public User register(String username, String password, String email, String fullName, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setEnabled(true);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> getUsersByEnabledStatus(Boolean enabled) {
        return userRepository.findByEnabled(enabled);
    }
    
    public List<User> getAvailableDeliveryStaff() {
        return userRepository.findAvailableDeliveryStaff();
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        userRepository.delete(user);
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        return userRepository.save(user);
    }
    
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    
    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole().name());
        userInfo.put("enabled", user.getEnabled());
        return userInfo;
    }
    
    public org.springframework.data.domain.Page<User> getAllUsersWithFilters(
            org.springframework.data.domain.Pageable pageable,
            String username,
            User.Role role,
            Boolean enabled) {
        
        org.springframework.data.jpa.domain.Specification<User> spec = 
                (root, query, criteriaBuilder) -> {
                    java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
                    
                    if (username != null && !username.trim().isEmpty()) {
                        predicates.add(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("username")),
                                "%" + username.toLowerCase() + "%"));
                    }
                    
                    if (role != null) {
                        predicates.add(criteriaBuilder.equal(
                                root.get("role"), role));
                    }
                    
                    if (enabled != null) {
                        predicates.add(criteriaBuilder.equal(
                                root.get("enabled"), enabled));
                    }
                    
                    return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
                };
        
        return userRepository.findAll(spec, pageable);
    }
}