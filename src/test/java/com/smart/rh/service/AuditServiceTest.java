package com.smart.rh.service;

import com.smart.rh.entity.AuditLog;
import com.smart.rh.entity.Role;
import com.smart.rh.entity.User;
import com.smart.rh.repository.AuditLogRepository;
import com.smart.rh.security.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuditService}.
 *
 * <p>SecurityContextHolder is manipulated directly because AuditService
 * reads it through the static API.  The context is cleared after every test
 * to prevent leakage between test methods.</p>
 *
 * <p>Note: {@code @Transactional(propagation=REQUIRES_NEW)} is a Spring AOP
 * concern and does NOT execute in a plain unit test.  The tests verify
 * the persistence behaviour (what gets saved) and error-swallowing behaviour.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository repository;

    @InjectMocks private AuditService auditService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ── log() — with authenticated SecurityContext ────────────────────────────

    @Test
    void log_withAuthenticatedContext_savesCorrectActionEntityAndActor() {
        setUpSecurityContext(42L, Role.ROLE_RH);

        auditService.log("CREATE", "Employe", 15L, "Employee Jean Dupont created");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getEntityName()).isEqualTo("Employe");
        assertThat(saved.getEntityId()).isEqualTo("15");
        assertThat(saved.getActorUserId()).isEqualTo(42L);
        assertThat(saved.getActorRole()).isEqualTo("ROLE_RH");
        assertThat(saved.getPayloadSummary()).isEqualTo("Employee Jean Dupont created");
        assertThat(saved.getIpAddress()).isNull();
        assertThat(saved.getTs()).isNotNull();
    }

    @Test
    void log_withIpAddress_savesIpInAuditLog() {
        setUpSecurityContext(10L, Role.ROLE_ADMIN);

        auditService.log("DELETE", "Poste", 5L, "Poste #5 deleted", "192.168.1.50");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("192.168.1.50");
        assertThat(captor.getValue().getAction()).isEqualTo("DELETE");
    }

    @Test
    void log_withEmptySecurityContext_actorFieldsAreNull() {
        // No security context set — anonymous/system call
        auditService.log("SEED", "Employe", null, "Initial seed data loaded");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getActorUserId()).isNull();
        assertThat(saved.getActorRole()).isNull();
        assertThat(saved.getEntityId()).isNull();   // entity id was null
        assertThat(saved.getAction()).isEqualTo("SEED");
    }

    @Test
    void log_withAdminContext_recordsAdminRole() {
        setUpSecurityContext(1L, Role.ROLE_ADMIN);

        auditService.log("UPDATE", "Contrat", 8L, "Salary updated");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getActorRole()).isEqualTo("ROLE_ADMIN");
        assertThat(captor.getValue().getActorUserId()).isEqualTo(1L);
    }

    // ── logAs() — explicit actor overload ─────────────────────────────────────

    @Test
    void logAs_storesExplicitActorIgnoringSecurityContext() {
        // Even if a different user is in the security context, logAs() uses explicit values
        setUpSecurityContext(99L, Role.ROLE_EMPLOYEE);

        auditService.logAs("LOGIN", "User", 7L,
                7L, "ROLE_RH", "Login from 10.0.0.1", "10.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getAction()).isEqualTo("LOGIN");
        assertThat(saved.getEntityName()).isEqualTo("User");
        assertThat(saved.getEntityId()).isEqualTo("7");
        assertThat(saved.getActorUserId()).isEqualTo(7L);          // explicit, not 99
        assertThat(saved.getActorRole()).isEqualTo("ROLE_RH");      // explicit, not EMPLOYEE
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(saved.getPayloadSummary()).contains("Login from");
    }

    @Test
    void logAs_loginFailed_savesNullActorIdAndRole() {
        auditService.logAs("LOGIN_FAILED", "User", null,
                null, null, "Failed login for: alice", "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getActorUserId()).isNull();
        assertThat(saved.getActorRole()).isNull();
        assertThat(saved.getEntityId()).isNull();
        assertThat(saved.getAction()).isEqualTo("LOGIN_FAILED");
        // Sensitive data rule: summary must NOT expose passwords
        assertThat(saved.getPayloadSummary()).doesNotContain("password");
    }

    @Test
    void logAs_register_containsUsernameButNotPassword() {
        auditService.logAs("REGISTER", "User", 5L,
                5L, "ROLE_EMPLOYEE", "Registered user: charlie (role=ROLE_EMPLOYEE)", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        String summary = captor.getValue().getPayloadSummary();

        assertThat(summary).contains("charlie");
        assertThat(summary).contains("ROLE_EMPLOYEE");
        assertThat(summary).doesNotContainIgnoringCase("password");
        assertThat(summary).doesNotContain("$2a$");  // no hashed password
    }

    // ── exception swallowing ─────────────────────────────────────────────────

    @Test
    void log_repositoryThrows_exceptionIsSwallowedAndDoesNotPropagateToCallers() {
        when(repository.save(any())).thenThrow(new RuntimeException("DB connection lost"));

        // Must NOT throw — audit failures must never disrupt HR operations
        assertThatNoException().isThrownBy(() ->
                auditService.log("CREATE", "Employe", 1L, "test summary"));
    }

    @Test
    void logAs_repositoryThrows_exceptionIsSwallowed() {
        when(repository.save(any())).thenThrow(new RuntimeException("Timeout"));

        assertThatNoException().isThrownBy(() ->
                auditService.logAs("LOGIN", "User", 1L,
                        1L, "ROLE_ADMIN", "Login OK", "127.0.0.1"));
    }

    // ── multiple operations in sequence ───────────────────────────────────────

    @Test
    void log_calledMultipleTimes_eachCallProducesDistinctAuditEntry() {
        setUpSecurityContext(5L, Role.ROLE_RH);

        auditService.log("CREATE", "Conge",    1L, "Leave created");
        auditService.log("APPROVE_LEAVE", "Conge", 1L, "Leave approved");
        auditService.log("CREATE", "Paie",     3L, "Payroll generated");

        verify(repository, times(3)).save(any(AuditLog.class));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository, times(3)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(AuditLog::getAction)
                .containsExactly("CREATE", "APPROVE_LEAVE", "CREATE");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void setUpSecurityContext(Long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        user.setEnabled(Boolean.TRUE);
        UserDetailsImpl ud = new UserDetailsImpl(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                ud, null, ud.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
