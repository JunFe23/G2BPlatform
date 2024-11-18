package org.example.g2bplatform.repository;

import org.example.g2bplatform.model.ContractInfoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoDetailRepository extends JpaRepository<ContractInfoDetail, Long> {
}
