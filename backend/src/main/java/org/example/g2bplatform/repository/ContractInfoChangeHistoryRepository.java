package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfoChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoChangeHistoryRepository extends JpaRepository<ContractInfoChangeHistory, Long> {
}
