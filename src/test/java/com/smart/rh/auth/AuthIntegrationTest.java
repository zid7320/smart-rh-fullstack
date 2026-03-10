package com.smart.rh.auth;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.auth.RegisterRequest;
import com.smart.rh.dto.auth.UserProfileDto;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack auth integration tests (H2 in-memory + embedded server).
 * Covers: registration, login (username + email), /me, 401 & 403 RBAC checks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired TestRestTemplate  restTemplate;
    @Autowired UserRepository    userRepository;
    @Autowired EmployeRepository employeRepository;
    @Autowired PasswordEncoder   passwordEncoder;

    private static final String ADMIN_UN  = "testadmin";
    private static final String ADMIN_PWD = "Admin@2024";
    private static final String EMP_UN    = "testemployee";
    private static final String EMP_PWD   = "Employee@2024";

    private String adminToken;
    private String empToken;

    @BeforeEach
    void setUp() {
        // FK-safe: Employe.user_id → Users; delete Employe first to avoid constraint violation
        // when other test classes (AttendanceIT, LeaveWorkflowIT, etc.) run before this one.
        employeRepository.deleteAll();
        userRepository.deleteAll();

        saveUser(ADMIN_UN, "admin@integration.test", ADMIN_PWD, Role.ROLE_ADMIN);
        saveUser(EMP_UN,   "emp@integration.test",   EMP_PWD,   Role.ROLE_EMPLOYEE);

        adminToken = loginAndGetToken(ADMIN_UN, ADMIN_PWD);
        empToken   = loginAndGetToken(EMP_UN,   EMP_PWD);
    }

    // ── Registration ─────────────────────────────────────────────────────────

    @Test
    void register_validPayload_returns201WithJwt() {
        var req = new RegisterRequest("newuser", "new@test.com", "Password@123");
        var res = restTemplate.postForEntity("/api/auth/register", req, AuthResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().token()).isNotBlank();
        assertThat(res.getBody().role()).isEqualTo("ROLE_EMPLOYEE");   // defaults to employee
    }

    @Test
    void register_duplicateUsername_returns400() {
        var req = new RegisterRequest(ADMIN_UN, "unique@test.com", "Password@123");
        var res = restTemplate.postForEntity("/api/auth/register", req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_duplicateEmail_returns400() {
        var req = new RegisterRequest("uniqueuser", "admin@integration.test", "Password@123");
        var res = restTemplate.postForEntity("/api/auth/register", req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_weakPassword_returns400() {
        var req = new RegisterRequest("weakpwd", "weak@test.com", "short");
        var res = restTemplate.postForEntity("/api/auth/register", req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withUsername_returns200AndJwt() {
        var res = postLogin(ADMIN_UN, ADMIN_PWD, AuthResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().token()).isNotBlank();
        assertThat(res.getBody().role()).isEqualTo("ROLE_ADMIN");
        assertThat(res.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(res.getBody().expiresIn()).isPositive();
    }

    @Test
    void login_withEmail_returns200AndJwt() {
        var res = postLogin("admin@integration.test", ADMIN_PWD, AuthResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().token()).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() {
        var res = postLogin(ADMIN_UN, "WrongPassword!", Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_unknownUser_returns401() {
        var res = postLogin("nobody", "Password@1234", Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── GET /api/auth/me ──────────────────────────────────────────────────────

    @Test
    void getMe_withValidAdminJwt_returnsProfile() {
        var res = authGet("/api/auth/me", adminToken, UserProfileDto.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().username()).isEqualTo(ADMIN_UN);
        assertThat(res.getBody().role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void getMe_withValidEmployeeJwt_returnsProfile() {
        var res = authGet("/api/auth/me", empToken, UserProfileDto.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().username()).isEqualTo(EMP_UN);
        assertThat(res.getBody().role()).isEqualTo("ROLE_EMPLOYEE");
    }

    @Test
    void getMe_withoutJwt_returns401() {
        var res = restTemplate.getForEntity("/api/auth/me", Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getMe_withMalformedJwt_returns401() {
        var res = authGet("/api/auth/me", "not.a.valid.token", Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── RBAC enforcement ──────────────────────────────────────────────────────

    @Test
    void adminEndpoint_withAdminJwt_returns200() {
        var res = authGet("/api/admin/ping", adminToken, Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void adminEndpoint_withEmployeeJwt_returns403() {
        var res = authGet("/api/admin/ping", empToken, Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminEndpoint_withoutJwt_returns401() {
        var res = restTemplate.getForEntity("/api/admin/ping", Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rhEndpoint_withAdminJwt_returns200() {
        var res = authGet("/api/rh/ping", adminToken, Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void rhEndpoint_withEmployeeJwt_returns403() {
        var res = authGet("/api/rh/ping", empToken, Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void saveUser(String username, String email, String pwd, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setRole(role);
        u.setEnabled(Boolean.TRUE);
        userRepository.save(u);
    }

    private String loginAndGetToken(String username, String password) {
        var res = postLogin(username, password, AuthResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        return res.getBody().token();
    }

    private <T> ResponseEntity<T> postLogin(String usernameOrEmail, String password,
                                             Class<T> type) {
        return restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest(usernameOrEmail, password),
                type);
    }

    private <T> ResponseEntity<T> authGet(String url, String token, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), type);
    }
}
