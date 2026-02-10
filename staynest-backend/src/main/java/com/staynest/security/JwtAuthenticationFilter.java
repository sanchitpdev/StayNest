package com.staynest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            //step 1: Extract JWT token form request header
            String jwt = getJwtFromRequest(request);

            //step 2: Check if token exist is valid
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                //step 3: Get userId from token
                UUID userId = tokenProvider.getUserIdFromToken(jwt);

                //step 4: Load user details from database
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                //step 5: Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                //step 6: set additional details (Ip.address session id)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                //step 7: Set authentication in spring  security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Set authentication for user: {}", userDetails.getUsername());
            }

        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        //step 8: Continue with the request
        filterChain.doFilter(request, response);

    }

    //Extract JWT token from Authorization header.
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
