package com.staynest.config;

import com.staynest.security.CustomUserDetailsService;
import com.staynest.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for StayNest API.
 *
 * Configures:
 * - JWT-based authentication
 * - Public and protected endpoints
 * - CORS
 * - CSRF (disabled for REST API)
 * - Stateless session management
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean.
     * Uses BCrypt with strength 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication provider bean.
     * Configures user details service and password encoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Main security filter chain.
     * Defines all public and protected endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless REST API)
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // SWAGGER & API DOCUMENTATION (PUBLIC)
                        // ========================================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/**"
                        ).permitAll()

                        // ========================================
                        // HEALTH CHECK ENDPOINTS (PUBLIC)
                        // ========================================
                        .requestMatchers("/health/**").permitAll()

                        // ========================================
                        // AUTHENTICATION ENDPOINTS (PUBLIC)
                        // ========================================
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        // ========================================
                        // PROPERTY ENDPOINTS (PUBLIC GET ONLY)
                        // ========================================
                        // Browse all properties (with pagination)
                        .requestMatchers(HttpMethod.GET,
                                "/properties",
                                "/properties/paginated"
                        ).permitAll()

                        // View single property details
                        .requestMatchers(HttpMethod.GET,
                                "/properties/*"
                        ).permitAll()

                        // View properties by host (public profile)
                        .requestMatchers(HttpMethod.GET,
                                "/properties/host/*"
                        ).permitAll()

                        // Search properties (public)
                        .requestMatchers(HttpMethod.GET,
                                "/properties/search",
                                "/properties/search/city"
                        ).permitAll()

                        // Advanced search (POST but public)
                        .requestMatchers(HttpMethod.POST,
                                "/properties/search/advanced"
                        ).permitAll()

                        // ========================================
                        // UNIT ENDPOINTS (PUBLIC GET ONLY)
                        // ========================================
                        .requestMatchers(HttpMethod.GET,
                                "/units/property/*",
                                "/units/property/*/available",
                                "/units/{unitId}"
                        ).permitAll()

                        // ========================================
                        // BOOKING ENDPOINTS (PUBLIC)
                        // ========================================
                        // Check availability (public)
                        .requestMatchers(HttpMethod.GET,
                                "/bookings/availability/*"
                        ).permitAll()

                        // ========================================
                        // REVIEW ENDPOINTS (PUBLIC GET ONLY)
                        // ========================================
                        .requestMatchers(HttpMethod.GET,
                                "/reviews",
                                "/reviews/*",
                                "/reviews/property/*",
                                "/reviews/property/*/average-rating",
                                "/reviews/user/*"
                        ).permitAll()

                        // ========================================
                        // IMAGE ENDPOINTS (PUBLIC GET ONLY)
                        // ========================================
                        .requestMatchers(HttpMethod.GET,
                                "/images/properties/*",
                                "/images/units/*"
                        ).permitAll()

                        // ========================================
                        // USER PROFILE ENDPOINTS (PUBLIC GET)
                        // ========================================
                        // View public user profiles
                        .requestMatchers(HttpMethod.GET,
                                "/users/*"
                        ).permitAll()

                        // ========================================
                        // ERROR HANDLING (PUBLIC)
                        // ========================================
                        .requestMatchers("/error").permitAll()

                        // ========================================
                        // ALL OTHER ENDPOINTS (PROTECTED)
                        // ========================================
                        // Requires JWT authentication:
                        // - POST/PUT/DELETE /properties/** (create/update/delete properties)
                        // - POST/PUT/DELETE /units/** (create/update/delete units)
                        // - POST/PUT/DELETE /bookings/** (create/cancel bookings)
                        // - POST /payments/** (create payments)
                        // - POST /reviews/** (create reviews)
                        // - POST/DELETE /wishlists/** (manage wishlists)
                        // - POST/DELETE /images/** (upload/delete images)
                        // - GET/PUT /users/me/** (own profile)
                        // - GET /dashboard/** (dashboard stats)
                        .anyRequest().authenticated()
                )

                // Stateless session (no server-side sessions)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Use custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}