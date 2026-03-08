package com.smart.rh.error;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.dto.conge.CongeRequest;
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

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that ALL error conditions produce a consistent {@link com.smart.rh.exception.ErrorResponse}
 * envelope with the expected fields: {@code timestamp}, {@code status}, {@code error},
 * {@code message}, {@code path}, and optionally {@code fieldErrors}.
 *
 * <h3>Coverage</h3>
 * <ul>
 *   <li>404 Not Found — standard envelope, no {@code fieldErrors}</li>
 *   <li>400 Validation — standard envelope + {@code fieldErrors} map</li>
 *   <li>400 Bad Request (explicit) — standard envelope, no {@code fieldErrors}</li>
 *   <li>401 Unauthorized — Spring Security entry point fires (no JWT)</li>
 *   <li>403 Forbidden — Spring Security access-denied handler fires (wrong role)</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErrorResponseIT {

    @Autowired TestRestTemplate  restTemplate;
    @Autowired UserRepository    userRepository;
    @Autowired EmployeRepository employeRepository;
    @Autowired PasswordEncoder   passwordEncoder;

    private String adminToken, empToken;

    @BeforeEach
    void setUp() {
        employeRepository.deleteAll();
        userRepository.deleteAll();

        saveUser("er_admin", "er_admin@test.com", "Admin@2024",    Role.ROLE_ADMIN);
        saveUser("er_emp",   "er_emp@test.com",   "Employee@2024", Role.ROLE_EMPLOYEE);

        adminToken = loginAndGetToken("er_admin", "Admin@2024");
        empToken   = loginAndGetToken("er_emp",   "Employee@2024");
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @Test
    void notFound_hasCorrectEnvelope() {
        var res = authGet("/api/leaves/999999", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        // All mandatory envelope fields must be present
        assertThat(body).containsKey("timestamp");
        assertThat(body).containsKey("status");
        assertThat(body).containsKey("error");
        assertThat(body).containsKey("message");
        assertThat(body).containsKey("path");
        // Values match expectations
        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("error").toString()).containsIgnoringCase("Not Found");
        assertThat(body.get("path").toString()).contains("/api/leaves/999999");
        // fieldErrors must NOT be present for a plain 404
        assertThat(body).doesNotContainKey("fieldErrors");
    }

    @Test
    void notFound_payroll_hasCorrectStatus() {
        var res = authGet("/api/payroll/999999", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body.get("status")).isEqualTo(404);
    }

    // ── 400 Validation ────────────────────────────────────────────────────────

    @Test
    void validationError_hasFieldErrors() {
        // CongeRequest with blank type triggers @NotBlank → fieldErrors
        var req = new CongeRequest(999L, "",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        var res = authPost("/api/leaves", adminToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error").toString()).containsIgnoringCase("Validation");
        assertThat(body).containsKey("fieldErrors");

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.get("fieldErrors");
        // "type" field violated @NotBlank
        assertThat(fieldErrors).containsKey("type");
    }

    @Test
    void validationError_multipleFields_allReported() {
        // AttendanceRequest with both required fields null → both reported
        var req = new AttendanceRequest(null, null, null, null, null, null);
        var res = authPost("/api/attendance/recognition", adminToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("fieldErrors");

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.get("fieldErrors");
        // employeId and type are both @NotNull
        assertThat(fieldErrors).containsKey("employeId");
        assertThat(fieldErrors).containsKey("type");
    }

    @Test
    void validationError_envelopeHasNoTimestampNull() {
        var req = new CongeRequest(null, null, null, null);
        var res = authPost("/api/leaves", adminToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        // timestamp is always present and non-null
        assertThat(body.get("timestamp")).isNotNull();
        assertThat(body.get("path")).isNotNull();
    }

    // ── 401 Unauthorized ─────────────────────────────────────────────────────

    @Test
    void noJwt_returns401() {
        var res = restTemplate.getForEntity("/api/auth/me", Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void malformedJwt_returns401() {
        var res = authGet("/api/auth/me", "not.a.valid.token", Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void noJwt_protectedEndpoint_returns401() {
        var res = restTemplate.getForEntity("/api/leaves", Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── 403 Forbidden ────────────────────────────────────────────────────────

    @Test
    void wrongRole_returns403() {
        // EMPLOYEE cannot access ADMIN-only endpoint
        var res = authGet("/api/admin/ping", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void wrongRole_attendanceHistory_returns403() {
        // EMPLOYEE cannot access attendance history
        var res = authGet("/api/attendance/history", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void wrongRole_leaveList_returns403() {
        // EMPLOYEE cannot list all leaves
        var res = authGet("/api/leaves", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Envelope consistency ──────────────────────────────────────────────────

    @Test
    void errorEnvelope_contentTypeIsJson() {
        var res = authGet("/api/leaves/999999", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getHeaders().getContentType()).isNotNull();
        assertThat(res.getHeaders().getContentType().toString())
                .startsWith(MediaType.APPLICATION_JSON_VALUE);
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
        var res = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest(username, password),
                AuthResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        return res.getBody().token();
    }

    private <T> ResponseEntity<T> authGet(String url, String token, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(headers), type);
    }

    private <T> ResponseEntity<T> authPost(String url, String token, Object body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body, headers), type);
    }
}
