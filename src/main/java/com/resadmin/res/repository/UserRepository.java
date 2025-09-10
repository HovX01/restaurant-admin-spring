package com.resadmin.res.repository;

import com.resadmin.res.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByRoleAndEnabledTrue(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.enabled = true")
    List<User> findByRolesAndEnabledTrue(@Param("roles") List<User.Role> roles);
    
    @Query("SELECT u FROM User u WHERE u.role = 'DELIVERY_STAFF' AND u.enabled = true")
    List<User> findAvailableDeliveryStaff();
    
    @Query("SELECT u FROM User u WHERE u.role = 'KITCHEN_STAFF' AND u.enabled = true")
    List<User> findAvailableKitchenStaff();
    
    List<User> findByEnabled(Boolean enabled);
}