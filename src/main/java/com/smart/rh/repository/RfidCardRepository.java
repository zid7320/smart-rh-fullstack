package com.smart.rh.repository;

import com.smart.rh.entity.RfidCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfidCardRepository extends JpaRepository<RfidCard, Long> {
    Optional<RfidCard> findByCardId(String cardId);

    List<RfidCard> findByEmployeeId(Long employeeId);

    List<RfidCard> findByIsActiveTrue();

    boolean existsByCardId(String cardId);
}
