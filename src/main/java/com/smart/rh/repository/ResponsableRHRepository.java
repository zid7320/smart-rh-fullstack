package com.smart.rh.repository;

import com.smart.rh.entity.ResponsableRH;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponsableRHRepository extends JpaRepository<ResponsableRH, Long> {

    Optional<ResponsableRH> findByUserId(Long userId);
}
