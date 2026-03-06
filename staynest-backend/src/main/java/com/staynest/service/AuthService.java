package com.staynest.service;

import com.staynest.dto.request.LoginRequest;
import com.staynest.dto.request.RegisterRequest;
import com.staynest.dto.response.AuthResponse;
import com.staynest.entity.User;
import com.staynest.exception.BadRequestException;
import com.staynest.repository.UserRepository;
import com.staynest.security.JwtTokenProvider;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register new user
     *
     * @Param registerRequest - Registration Data
     * @return  AuthResponse -  Jwt token and user info
     */

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest){
        logger.info("Registration new user with email: {}",registerRequest.getEmail());

        //step 1 : check if email already exists
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            logger.warn("Registration failed: Email already exists - {}",registerRequest.getEmail());
            throw new BadRequestException("Email already exists");
        }

        //step 2: Create new user entity
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(registerRequest.getRole())
                .isVerified(false)
                .build();

        //step 3: Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User registration successfully with ID: {}",savedUser.getUserId());

        //step 4: Generate JWT token
        String token = jwtTokenProvider.generateTokenFromUser(savedUser);

        //step 5: Build and return response
        return AuthResponse.builder()
                .token(token)
                .type("Barer")
                .userId(savedUser.getUserId().toString())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .build();
    }

    /**
     * Login an existing user
     *
     * @Param loginRequest - Login credentials
     * @return AuthResponse - JWT token and user info
     */

    public AuthResponse loginUser(LoginRequest loginRequest){
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        //step1 : Authenticate user(checks email+password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        //step 2: Set authentication in context (marks user as authenticated)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //step 3: Get authenticated user
        User user = (User) authentication.getPrincipal();

        //step 4: Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        //step 5: Build and return response
        return AuthResponse.builder()
                .token(token)
                .type("Barer")
                .userId(user.getUserId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .build();
    }
}
