package org.example.g2bplatform.Repostiory;

import org.example.g2bplatform.Model.ContractInfoChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoChangeHistoryRepository extends JpaRepository<ContractInfoChangeHistory, Long> {
}
