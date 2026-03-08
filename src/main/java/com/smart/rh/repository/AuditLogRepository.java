package com.smart.rh.repository;

import com.smart.rh.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** All audit entries, newest first. */
    Page<AuditLog> findAllByOrderByTsDesc(Pageable pageable);

    /** All events performed by a specific user (actor). */
    Page<AuditLog> findByActorUserIdOrderByTsDesc(Long actorUserId, Pageable pageable);

    /** All events on a given entity type (e.g. "Employe", "Conge"). */
    Page<AuditLog> findByEntityNameOrderByTsDesc(String entityName, Pageable pageable);

    /**
     * All events touching one specific resource instance
     * (e.g. all changes to Employe #42).
     */
    Page<AuditLog> findByEntityNameAndEntityIdOrderByTsDesc(
            String entityName, String entityId, Pageable pageable);

    /** All events of a given action type (e.g. "DELETE", "APPROVE_LEAVE"). */
    Page<AuditLog> findByActionOrderByTsDesc(String action, Pageable pageable);
}
