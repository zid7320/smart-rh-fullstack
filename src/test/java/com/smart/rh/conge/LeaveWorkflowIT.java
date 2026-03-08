package com.smart.rh.conge;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.conge.CongeRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.CongeRepository;
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
 * Integration tests for the leave (conge) workflow:
 * create → approve / reject → error cases → pagination.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LeaveWorkflowIT {

    @Autowired TestRestTemplate  restTemplate;
    @Autowired UserRepository    userRepository;
    @Autowired EmployeRepository employeRepository;
    @Autowired CongeRepository   congeRepository;
    @Autowired PasswordEncoder   passwordEncoder;

    private String adminToken, rhToken, empToken;
    private Long   testEmployeId;

    private static final String ADMIN_UN  = "lv_admin";
    private static final String ADMIN_PWD = "Admin@2024";
    private static final String RH_UN     = "lv_rh";
    private static final String RH_PWD    = "Rh@Password1";
    private static final String EMP_UN    = "lv_employee";
    private static final String EMP_PWD   = "Employee@2024";

    @BeforeEach
    void setUp() {
        // FK-safe cleanup: Employe cascades to Conge, Paie, etc.
        employeRepository.deleteAll();
        userRepository.deleteAll();

        saveUser(ADMIN_UN, "lv_admin@test.com", ADMIN_PWD, Role.ROLE_ADMIN);
        saveUser(RH_UN,    "lv_rh@test.com",    RH_PWD,   Role.ROLE_RH);
        User empUser = saveUser(EMP_UN, "lv_emp@test.com", EMP_PWD, Role.ROLE_EMPLOYEE);

        Employe emp = new Employe();
        emp.setNom("Leave");
        emp.setPrenom("Test");
        emp.setEmail("lv_emp_profile@test.com");
        emp.setUser(empUser);
        testEmployeId = employeRepository.save(emp).getId();

        adminToken = loginAndGetToken(ADMIN_UN, ADMIN_PWD);
        rhToken    = loginAndGetToken(RH_UN,    RH_PWD);
        empToken   = loginAndGetToken(EMP_UN,   EMP_PWD);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Test
    void createLeave_asEmployee_returns201() {
        var req = new CongeRequest(testEmployeId, "ANNUAL",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        var res = authPost("/api/leaves", empToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createLeave_missingFields_returns400() {
        // type is blank — validation should reject
        var req = new CongeRequest(testEmployeId, "",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        var res = authPost("/api/leaves", empToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createLeave_withoutAuth_returns401() {
        var req = new CongeRequest(testEmployeId, "ANNUAL",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var res = restTemplate.exchange("/api/leaves", HttpMethod.POST,
                new HttpEntity<>(req, headers), Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── Approve / Reject ─────────────────────────────────────────────────────

    @Test
    void approveLeave_asRH_returns200AndApprovedStatut() {
        Long congeId = createLeave();

        var res = authPut("/api/leaves/" + congeId + "/approve", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("statut").toString()).containsIgnoringCase("APPROV");
    }

    @Test
    void rejectLeave_asRH_returns200AndRejectedStatut() {
        Long congeId = createLeave();

        var res = authPut("/api/leaves/" + congeId + "/reject", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("statut").toString()).containsIgnoringCase("REJECT");
    }

    @Test
    void approveLeave_asAdmin_returns200() {
        Long congeId = createLeave();

        var res = authPut("/api/leaves/" + congeId + "/approve", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void doubleApprove_returns4xx() {
        Long congeId = createLeave();
        authPut("/api/leaves/" + congeId + "/approve", rhToken, Object.class);

        // Second approval on an already-decided leave should be a client error
        var res = authPut("/api/leaves/" + congeId + "/approve", rhToken, Object.class);

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void approveLeave_asEmployee_returns403() {
        Long congeId = createLeave();

        var res = authPut("/api/leaves/" + congeId + "/approve", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Not Found ─────────────────────────────────────────────────────────────

    @Test
    void getById_nonExistent_returns404() {
        var res = authGet("/api/leaves/999999", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void approveNonExistent_returns404() {
        var res = authPut("/api/leaves/999999/approve", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    @Test
    void listPaginated_asAdmin_returns200WithPageStructure() {
        createLeave();

        var res = authGet("/api/leaves?page=0&size=10", adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("content");
        assertThat(body).containsKey("totalElements");
        assertThat(body).containsKey("totalPages");
    }

    @Test
    void listPaginated_asEmployee_returns403() {
        var res = authGet("/api/leaves", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void byEmployeePaginated_returns200() {
        createLeave();

        var res = authGet("/api/leaves/byEmployee/" + testEmployeId + "?page=0&size=5",
                adminToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).isNotNull();
        assertThat(body).containsKey("content");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long createLeave() {
        var req = new CongeRequest(testEmployeId, "ANNUAL",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(10));
        @SuppressWarnings("unchecked")
        var res = authPost("/api/leaves", empToken, req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Object idObj = res.getBody().get("id");
        return Long.valueOf(idObj.toString());
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

    private <T> ResponseEntity<T> authPut(String url, String token, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(headers), type);
    }
}
