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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 * No Spring context — all collaborators are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private AuditService          auditService;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtService            jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_withValidRequest_savesHashedPasswordAndReturnsJwt() {
        var request = new RegisterRequest("alice", "alice@test.com", "Secret@123");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("$bcrypt$hashed$");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn("jwt.token.here");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt.token.here");
        assertThat(response.role()).isEqualTo("ROLE_EMPLOYEE");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.expiresIn()).isEqualTo(86_400_000L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("$bcrypt$hashed$");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_EMPLOYEE);   // default when role=null
        assertThat(savedUser.getEnabled()).isTrue();

        verify(auditService).logAs(eq("REGISTER"), eq("User"), any(),
                any(), any(), contains("alice"), isNull());
    }

    @Test
    void register_publicApi_alwaysAssignsEmployeeRole() {
        var request = new RegisterRequest("boss", "boss@test.com", "Secret@123");

        when(userRepository.existsByUsername("boss")).thenReturn(false);
        when(userRepository.existsByEmail("boss@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$hash$");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("admin.jwt");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.register(request);

        assertThat(response.role()).isEqualTo("ROLE_EMPLOYEE");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ROLE_EMPLOYEE);
    }

    @Test
    void register_duplicateUsername_throwsBadRequestWithoutSaving() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        var request = new RegisterRequest("alice", "alice@test.com", "Secret@123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("alice");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsBadRequestWithoutSaving() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);
        var request = new RegisterRequest("newuser", "alice@test.com", "Secret@123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("alice@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_plainTextPasswordMustNeverBeSaved() {
        var request = new RegisterRequest("charlie", "charlie@test.com", "PlainText@1");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("PlainText@1")).thenReturn("$2a$hashed$");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("tok");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        // The plain-text password must NEVER be stored — only the encoded value
        assertThat(captor.getValue().getPassword()).doesNotContain("PlainText@1");
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$hashed$");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returnsJwtAndAuditsLogin() {
        User user = makeUser(5L, "alice", "alice@test.com", "$hashed$", Role.ROLE_EMPLOYEE);
        UserDetailsImpl ud = new UserDetailsImpl(user);
        var authToken = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(ud)).thenReturn("login.jwt");
        when(jwtService.getExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.login(
                new LoginRequest("alice", "Secret@123"), "192.168.1.1");

        assertThat(response.token()).isEqualTo("login.jwt");
        assertThat(response.role()).isEqualTo("ROLE_EMPLOYEE");
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(auditService).logAs(eq("LOGIN"), eq("User"), eq(5L),
                eq(5L), eq("ROLE_EMPLOYEE"), anyString(), eq("192.168.1.1"));
    }

    @Test
    void login_withBadCredentials_throwsAndAuditsLoginFailed() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("alice", "wrong"), "10.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        verify(auditService).logAs(eq("LOGIN_FAILED"), eq("User"),
                isNull(), isNull(), isNull(), anyString(), eq("10.0.0.1"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_withBadCredentials_doesNotExposeAttemptedPassword() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() ->
                authService.login(new LoginRequest("alice", "s3creT!pass"), "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        ArgumentCaptor<String> summaryCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).logAs(eq("LOGIN_FAILED"), eq("User"),
                isNull(), isNull(), isNull(), summaryCaptor.capture(), anyString());
        assertThat(summaryCaptor.getValue()).doesNotContain("s3creT!pass");
    }

    // ── getMe ─────────────────────────────────────────────────────────────────

    @Test
    void getMe_withAdminPrincipal_returnsFullProfile() {
        User user = makeUser(7L, "bob", "bob@test.com", "$pwd$", Role.ROLE_ADMIN);
        UserDetailsImpl ud = new UserDetailsImpl(user);

        UserProfileDto profile = authService.getMe(ud);

        assertThat(profile.id()).isEqualTo(7L);
        assertThat(profile.username()).isEqualTo("bob");
        assertThat(profile.email()).isEqualTo("bob@test.com");
        assertThat(profile.role()).isEqualTo("ROLE_ADMIN");
        assertThat(profile.enabled()).isTrue();
    }

    @Test
    void getMe_withEmployeePrincipal_returnsEmployeeRole() {
        User user = makeUser(9L, "carol", "carol@test.com", "$pwd$", Role.ROLE_EMPLOYEE);
        UserDetailsImpl ud = new UserDetailsImpl(user);

        UserProfileDto profile = authService.getMe(ud);

        assertThat(profile.role()).isEqualTo("ROLE_EMPLOYEE");
        assertThat(profile.username()).isEqualTo("carol");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User makeUser(Long id, String username, String email, String pwd, Role role) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(pwd);
        u.setRole(role);
        u.setEnabled(Boolean.TRUE);
        return u;
    }
}
