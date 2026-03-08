package com.smart.rh.attendance;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.entity.AttendanceType;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.AttendanceRepository;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for attendance recognition and history endpoints.
 *
 * <h3>Access rules verified</h3>
 * <ul>
 *   <li>POST /recognition — ADMIN or RH only</li>
 *   <li>GET  /history     — ADMIN or RH only</li>
 *   <li>GET  /byEmployee/{id} — ADMIN/RH: any; EMPLOYEE: self only</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AttendanceIT {

    @Autowired TestRestTemplate    restTemplate;
    @Autowired UserRepository      userRepository;
    @Autowired EmployeRepository   employeRepository;
    @Autowired AttendanceRepository attendanceRepository;
    @Autowired PasswordEncoder     passwordEncoder;

    private String rhToken, emp1Token, emp2Token;
    private Long   emp1Id, emp2Id;

    private static final String RH_UN    = "at_rh";
    private static final String RH_PWD   = "Rh@Password1";
    private static final String EMP1_UN  = "at_emp1";
    private static final String EMP1_PWD = "Employee@2024";
    private static final String EMP2_UN  = "at_emp2";
    private static final String EMP2_PWD = "Employee@2025";

    @BeforeEach
    void setUp() {
        // FK-safe cleanup: Employe cascades to Attendance, Conge, etc.
        employeRepository.deleteAll();
        userRepository.deleteAll();

        saveUser(RH_UN,   "at_rh@test.com",   RH_PWD,   Role.ROLE_RH);
        User emp1User = saveUser(EMP1_UN, "at_emp1@test.com", EMP1_PWD, Role.ROLE_EMPLOYEE);
        User emp2User = saveUser(EMP2_UN, "at_emp2@test.com", EMP2_PWD, Role.ROLE_EMPLOYEE);

        // emp1 is linked to a User → AttendanceSecurity.isSelf() can resolve it
        Employe emp1 = new Employe();
        emp1.setNom("Attendance");
        emp1.setPrenom("One");
        emp1.setEmail("at_emp1_profile@test.com");
        emp1.setUser(emp1User);
        emp1Id = employeRepository.save(emp1).getId();

        // emp2 is linked to a different User → emp1Token cannot see emp2's records
        Employe emp2 = new Employe();
        emp2.setNom("Attendance");
        emp2.setPrenom("Two");
        emp2.setEmail("at_emp2_profile@test.com");
        emp2.setUser(emp2User);
        emp2Id = employeRepository.save(emp2).getId();

        rhToken   = loginAndGetToken(RH_UN,   RH_PWD);
        emp1Token = loginAndGetToken(EMP1_UN, EMP1_PWD);
        emp2Token = loginAndGetToken(EMP2_UN, EMP2_PWD);
    }

    // ── POST /recognition ────────────────────────────────────────────────────

    @Test
    void recognize_asRH_returns201() {
        var req = new AttendanceRequest(emp1Id, AttendanceType.IN, null, 0.97, "CAM-01", "SITE-A");
        var res = authPost("/api/attendance/recognition", rhToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("type").toString()).isEqualTo("IN");
        assertThat(body.get("employeId")).isNotNull();
    }

    @Test
    void recognize_asEmployee_returns403() {
        var req = new AttendanceRequest(emp1Id, AttendanceType.IN, null, 0.95, "CAM-01", "SITE-A");
        var res = authPost("/api/attendance/recognition", emp1Token, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void recognize_withoutAuth_returns401() {
        var req = new AttendanceRequest(emp1Id, AttendanceType.IN, null, 0.95, "CAM-01", "SITE-A");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var res = restTemplate.exchange("/api/attendance/recognition", HttpMethod.POST,
                new HttpEntity<>(req, headers), Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void recognize_missingRequiredFields_returns400() {
        // employeId is null → @NotNull validation fails
        var req = new AttendanceRequest(null, null, null, null, null, null);
        var res = authPost("/api/attendance/recognition", rhToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── GET /history ──────────────────────────────────────────────────────────

    @Test
    void getHistory_asRH_returns200WithPageStructure() {
        postRecognition(emp1Id, AttendanceType.IN);

        var res = authGet("/api/attendance/history?page=0&size=10", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("content");
        assertThat(body).containsKey("totalElements");
        assertThat(body).containsKey("totalPages");
    }

    @Test
    void getHistory_asEmployee_returns403() {
        var res = authGet("/api/attendance/history", emp1Token, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── GET /byEmployee/{id} ──────────────────────────────────────────────────

    @Test
    void byEmployee_asRH_anyEmployee_returns200() {
        postRecognition(emp1Id, AttendanceType.OUT);

        var res = authGet("/api/attendance/byEmployee/" + emp1Id, rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("content");
    }

    @Test
    void byEmployee_asOwnEmployee_returns200() {
        // emp1 can access their own attendance records (isSelf = true)
        postRecognition(emp1Id, AttendanceType.IN);

        var res = authGet("/api/attendance/byEmployee/" + emp1Id, emp1Token, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void byEmployee_asOtherEmployee_returns403() {
        // emp1 cannot access emp2's attendance records (isSelf = false)
        var res = authGet("/api/attendance/byEmployee/" + emp2Id, emp1Token, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void byEmployee_withoutAuth_returns401() {
        var res = restTemplate.getForEntity(
                "/api/attendance/byEmployee/" + emp1Id, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getHistory_paginationDefaultSize_returns200() {
        // POST several records and verify pagination works
        for (int i = 0; i < 3; i++) {
            postRecognition(emp1Id, i % 2 == 0 ? AttendanceType.IN : AttendanceType.OUT);
        }

        var res = authGet("/api/attendance/history?page=0&size=2", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat((List<?>) body.get("content")).hasSize(2);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void postRecognition(Long employeId, AttendanceType type) {
        var req = new AttendanceRequest(employeId, type, null, 0.95, "CAM-01", "SITE-A");
        var res = authPost("/api/attendance/recognition", rhToken, req, Object.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private User saveUser(String username, String email, String pwd, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setRole(role);
        u.setEnabled(Boolean.TRUE);
        return userRepository.save(u);
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
