package org.example.g2bplatform.Repository;

import org.example.g2bplatform.Model.ContractInfoCnstwk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoCnstwkRepository extends JpaRepository<ContractInfoCnstwk, Long> {
}