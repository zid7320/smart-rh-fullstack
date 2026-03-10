package com.smart.rh.service;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.auth.RegisterRequest;
import com.smart.rh.dto.auth.UserProfileDto;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.repository.UserRepository;
import com.smart.rh.security.JwtService;
import com.smart.rh.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final AuditService          auditService;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // Register — always ROLE_EMPLOYEE
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_EMPLOYEE);
        user.setEnabled(Boolean.TRUE);

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(new UserDetailsImpl(saved));

        auditService.logAs("REGISTER", "User", saved.getId(),
                saved.getId(), saved.getRole().name(),
                "Registered user: " + saved.getUsername() + " (role=ROLE_EMPLOYEE)",
                null);

        return toResponse(token, saved);
    }

    // Login
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.usernameOrEmail(), request.password()));

            UserDetailsImpl ud   = (UserDetailsImpl) auth.getPrincipal();
            User             user = ud.getUser();
            String           token = jwtService.generateToken(ud);

            auditService.logAs("LOGIN", "User", user.getId(),
                    user.getId(), user.getRole().name(),
                    "Login from " + ipAddress,
                    ipAddress);

            return toResponse(token, user);

        } catch (BadCredentialsException ex) {
            auditService.logAs("LOGIN_FAILED", "User", null,
                    null, null,
                    "Failed login attempt for identifier: " + request.usernameOrEmail(),
                    ipAddress);
            throw ex;
        }
    }

    // Me
    public UserProfileDto getMe(UserDetailsImpl ud) {
        User u = ud.getUser();
        return new UserProfileDto(u.getId(), u.getUsername(),
                                  u.getEmail(), u.getRole().name(), u.getEnabled());
    }

    private AuthResponse toResponse(String token, User user) {
        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
