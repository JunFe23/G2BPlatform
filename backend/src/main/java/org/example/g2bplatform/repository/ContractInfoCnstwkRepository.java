package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfoCnstwk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContractInfoCnstwkRepository extends JpaRepository<ContractInfoCnstwk, Long> {
    void deleteByRgstDtBetween(LocalDateTime start, LocalDateTime end);
}
