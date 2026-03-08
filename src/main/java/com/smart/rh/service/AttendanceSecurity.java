package com.smart.rh.service;

import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * SpEL security bean that provides the "self access" predicate for attendance.
 *
 * <p>Used in {@code @PreAuthorize} expressions as:
 * <pre>
 *   @PreAuthorize("hasAnyRole('ADMIN','RH') or @attendanceSecurity.isSelf(authentication, #employeId)")
 * </pre>
 * </p>
 *
 * <p>An employee is considered "self" when the {@link com.smart.rh.entity.Employe} record
 * linked to the authenticated {@link com.smart.rh.entity.User} has the same ID as the
 * {@code employeId} path variable.</p>
 */
@Component("attendanceSecurity")
@RequiredArgsConstructor
public class AttendanceSecurity {

    private final EmployeRepository employeRepository;

    /**
     * Returns {@code true} when the authenticated user owns the Employe record
     * identified by {@code employeId}.
     *
     * @param authentication the current Spring Security authentication token
     * @param employeId      the Employe.id from the URL path variable
     */
    public boolean isSelf(Authentication authentication, Long employeId) {
        if (authentication == null || employeId == null) return false;
        if (!(authentication.getPrincipal() instanceof UserDetailsImpl ud)) return false;
        Long userId = ud.getUser().getId();
        return employeRepository.findByUser_Id(userId)
                .map(emp -> emp.getId().equals(employeId))
                .orElse(false);
    }
}
