package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfoServc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ContractInfoServcRepository extends JpaRepository<ContractInfoServc, Long> {
    void deleteByRgstDtBetween(LocalDateTime start, LocalDateTime end);
}
