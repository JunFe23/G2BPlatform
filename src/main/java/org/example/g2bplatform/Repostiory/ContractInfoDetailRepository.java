package org.example.g2bplatform.Repostiory;

import org.example.g2bplatform.Model.ContractInfoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoDetailRepository extends JpaRepository<ContractInfoDetail, Long> {
}
