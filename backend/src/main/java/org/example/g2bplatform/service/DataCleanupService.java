package org.example.g2bplatform.service;

import jakarta.transaction.Transactional;
import org.example.g2bplatform.repository.ContractInfoCnstwkRepository;
import org.example.g2bplatform.repository.ContractInfoDetailRepository;
import org.example.g2bplatform.repository.ContractInfoRepository;
import org.example.g2bplatform.repository.ContractInfoServcRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DataCleanupService {
    private final ContractInfoRepository contractInfoRepository;
    private final ContractInfoDetailRepository contractInfoDetailRepository;
    private final ContractInfoCnstwkRepository contractInfoCnstwkRepository;
    private final ContractInfoServcRepository contractInfoServcRepository;

    public DataCleanupService(ContractInfoRepository a,
                              ContractInfoDetailRepository b,
                              ContractInfoCnstwkRepository c,
                              ContractInfoServcRepository d) {
        this.contractInfoRepository = a;
        this.contractInfoDetailRepository = b;
        this.contractInfoCnstwkRepository = c;
        this.contractInfoServcRepository = d;
    }

    @Transactional
    public void deleteByRgstDtRange(String endpoint, LocalDateTime start, LocalDateTime end) {

        if (endpoint.contains("ThngDetail")) {
            contractInfoDetailRepository.deleteByRgstDtBetween(start, end);
        } else if (endpoint.contains("Thng")) {
            contractInfoRepository.deleteByRgstDtBetween(start, end);
        } else if (endpoint.contains("Cnstwk")) {
            contractInfoCnstwkRepository.deleteByRgstDtBetween(start, end);
        } else if (endpoint.contains("Servc")) {
            contractInfoServcRepository.deleteByRgstDtBetween(start, end);
        }
    }

}

