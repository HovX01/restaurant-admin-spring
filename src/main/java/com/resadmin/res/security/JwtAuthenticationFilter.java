package com.resadmin.res.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resadmin.res.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Skip JWT processing for non-API routes
        if (!requestPath.startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.error("JWT Token has expired", e);
                handleAuthenticationError(response, "TOKEN_EXPIRED", "JWT token has expired", 401);
                return;
            } catch (MalformedJwtException e) {
                log.error("JWT Token is malformed", e);
                handleAuthenticationError(response, "MALFORMED_TOKEN", "JWT token is malformed", 401);
                return;
            } catch (SignatureException e) {
                log.error("JWT Token signature is invalid", e);
                handleAuthenticationError(response, "INVALID_SIGNATURE", "JWT token signature is invalid", 401);
                return;
            } catch (Exception e) {
                log.error("Cannot process JWT Token", e);
                handleAuthenticationError(response, "INVALID_TOKEN", "JWT token is invalid", 401);
                return;
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.error("JWT Token validation failed for user: {}", username);
                    handleAuthenticationError(response, "TOKEN_VALIDATION_FAILED", "JWT token validation failed", 401);
                    return;
                }
            } catch (UsernameNotFoundException e) {
                log.error("User not found: {}", username, e);
                handleAuthenticationError(response, "USER_NOT_FOUND", "User not found", 401);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void handleAuthenticationError(HttpServletResponse response, String errorCode, 
                                         String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}