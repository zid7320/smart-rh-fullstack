package com.smart.rh.paie;

import com.smart.rh.dto.auth.AuthResponse;
import com.smart.rh.dto.auth.LoginRequest;
import com.smart.rh.dto.paie.PayrollGenerateRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.PaieRepository;
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
 * Integration tests for the payroll endpoints:
 * generate / list / get-by-id / PDF / 403 / 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PayrollIT {

    @Autowired TestRestTemplate  restTemplate;
    @Autowired UserRepository    userRepository;
    @Autowired EmployeRepository employeRepository;
    @Autowired PaieRepository    paieRepository;
    @Autowired PasswordEncoder   passwordEncoder;

    private String rhToken, empToken;
    private Long   testEmployeId;

    private static final String RH_UN   = "pr_rh";
    private static final String RH_PWD  = "Rh@Password1";
    private static final String EMP_UN  = "pr_emp";
    private static final String EMP_PWD = "Employee@2024";

    @BeforeEach
    void setUp() {
        // FK-safe cleanup: Employe cascades to Paie, Contrat, etc.
        employeRepository.deleteAll();
        userRepository.deleteAll();

        saveUser(RH_UN,  "pr_rh@test.com",  RH_PWD,  Role.ROLE_RH);
        User empUser = saveUser(EMP_UN, "pr_emp@test.com", EMP_PWD, Role.ROLE_EMPLOYEE);

        Employe emp = new Employe();
        emp.setNom("Payroll");
        emp.setPrenom("Test");
        emp.setEmail("pr_emp_profile@test.com");
        emp.setUser(empUser);
        testEmployeId = employeRepository.save(emp).getId();

        rhToken  = loginAndGetToken(RH_UN,  RH_PWD);
        empToken = loginAndGetToken(EMP_UN, EMP_PWD);
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    @Test
    void generate_asRH_returns201WithList() {
        var req = new PayrollGenerateRequest(3, 2025);
        var res = authPost("/api/payroll/generate", rhToken, req, List.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotNull();
        // At least one record created for the test employee
        assertThat((List<?>) res.getBody()).isNotEmpty();
    }

    @Test
    void generate_asEmployee_returns403() {
        var req = new PayrollGenerateRequest(3, 2025);
        var res = authPost("/api/payroll/generate", empToken, req, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void generate_missingFields_returns400() {
        // Null body should produce validation 400
        var res = authPost("/api/payroll/generate", rhToken, new PayrollGenerateRequest(null, null), Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void generate_idempotent_skipsExistingRecords() {
        var req = new PayrollGenerateRequest(4, 2025);
        authPost("/api/payroll/generate", rhToken, req, List.class);

        // Second run for same month/year must not create duplicates
        @SuppressWarnings("unchecked")
        var res = (ResponseEntity<List<?>>) (ResponseEntity<?>) authPost("/api/payroll/generate", rhToken, req, List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isEmpty();   // nothing new created
    }

    // ── List ─────────────────────────────────────────────────────────────────

    @Test
    void listAll_asRH_returns200WithPageStructure() {
        authPost("/api/payroll/generate", rhToken, new PayrollGenerateRequest(5, 2025), List.class);

        var res = authGet("/api/payroll?page=0&size=10", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("content");
        assertThat(body).containsKey("totalElements");
    }

    @Test
    void listAll_asEmployee_returns403() {
        var res = authGet("/api/payroll", empToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── Get by ID ─────────────────────────────────────────────────────────────

    @Test
    void getById_existing_returns200() {
        Long paieId = generateAndGetFirstId(6, 2025);

        var res = authGet("/api/payroll/" + paieId, rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("id");
        assertThat(body.get("mois")).isEqualTo(6);
        assertThat(body.get("annee")).isEqualTo(2025);
    }

    @Test
    void getById_nonExistent_returns404() {
        var res = authGet("/api/payroll/999999", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    @Test
    void getPdf_returns200WithApplicationPdfContentType() {
        Long paieId = generateAndGetFirstId(7, 2025);

        var res = authGet("/api/payroll/" + paieId + "/pdf", rhToken, byte[].class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getHeaders().getContentType()).isNotNull();
        assertThat(res.getHeaders().getContentType().toString())
                .startsWith(MediaType.APPLICATION_PDF_VALUE);
        assertThat(res.getBody()).isNotEmpty();
    }

    @Test
    void getPdf_asEmployee_withOwnRecord_returns200() {
        // Employees can see their own payslip (isAuthenticated())
        Long paieId = generateAndGetFirstId(8, 2025);

        var res = authGet("/api/payroll/" + paieId + "/pdf", empToken, byte[].class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPdf_nonExistent_returns404() {
        var res = authGet("/api/payroll/999999/pdf", rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void byEmployee_returns200WithPageStructure() {
        authPost("/api/payroll/generate", rhToken, new PayrollGenerateRequest(9, 2025), List.class);

        var res = authGet("/api/payroll/byEmployee/" + testEmployeId + "?page=0&size=10",
                rhToken, Object.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsKey("content");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Generate payroll for the given month/year and return the ID of the first record.
     */
    @SuppressWarnings("unchecked")
    private Long generateAndGetFirstId(int mois, int annee) {
        var res = authPost("/api/payroll/generate", rhToken,
                new PayrollGenerateRequest(mois, annee), List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((List<?>) res.getBody()).isNotEmpty();

        Map<String, Object> first = (Map<String, Object>) res.getBody().get(0);
        return Long.valueOf(first.get("id").toString());
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
