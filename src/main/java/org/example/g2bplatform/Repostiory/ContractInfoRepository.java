package org.example.g2bplatform.Repostiory;

import org.example.g2bplatform.Model.ContractInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractInfoRepository extends JpaRepository<ContractInfo, Long> {
}
