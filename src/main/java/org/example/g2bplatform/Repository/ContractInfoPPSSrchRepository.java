package org.example.g2bplatform.Repository;

import org.example.g2bplatform.Model.ContractInfoPPSSrch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoPPSSrchRepository extends JpaRepository<ContractInfoPPSSrch, Long> {
}
