package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfoPPSSrch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoPPSSrchRepository extends JpaRepository<ContractInfoPPSSrch, Long> {
}
