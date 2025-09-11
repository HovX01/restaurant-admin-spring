package com.resadmin.res.config;

import com.resadmin.res.security.CustomAuthEntryPoint;
import com.resadmin.res.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CustomAuthEntryPoint customAuthEntryPoint;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // DaoAuthenticationProvider is automatically configured by Spring Boot 3.x
    // when UserDetailsService and PasswordEncoder beans are present
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow unrestricted access to all non-API routes
                .requestMatchers("/", "/index.html", "/static/**", "/public/**", "/assets/**", "/favicon.ico").permitAll()
                
                // Swagger/OpenAPI endpoints
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Public API endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Manager and Admin endpoints
                .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/orders/create").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/orders/*/status").hasAnyRole("ADMIN", "MANAGER", "KITCHEN_STAFF")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "KITCHEN_STAFF")
                .requestMatchers("/api/deliveries/assign").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/deliveries/**").hasAnyRole("ADMIN", "MANAGER", "DELIVERY_STAFF")
                
                // Kitchen staff endpoints
                .requestMatchers("/api/orders/kitchen/**").hasAnyRole("ADMIN", "MANAGER", "KITCHEN_STAFF")
                
                // Delivery staff endpoints
                .requestMatchers("/api/deliveries/my/**").hasAnyRole("ADMIN", "MANAGER", "DELIVERY_STAFF")
                
                // Allow all other non-API requests
                .anyRequest().permitAll()
            )
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customAuthEntryPoint))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}