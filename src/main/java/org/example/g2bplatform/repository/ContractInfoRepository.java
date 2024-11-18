package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoRepository extends JpaRepository<ContractInfo, Long> {
}
