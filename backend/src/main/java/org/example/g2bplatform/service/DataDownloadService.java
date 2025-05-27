package org.example.g2bplatform.service;

import jakarta.transaction.Transactional;
import org.example.g2bplatform.DTO.ContractInfoCnstwkDTO;
import org.example.g2bplatform.DTO.ContractInfoDTO;
import org.example.g2bplatform.DTO.ContractInfoDetailDTO;
import org.example.g2bplatform.DTO.ContractInfoServcDTO;
import org.example.g2bplatform.mapper.DataDownloadMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DataDownloadService {

    private final DataDownloadMapper dataDownloadMapper;

    public DataDownloadService(DataDownloadMapper dataDownloadMapper) {
        this.dataDownloadMapper = dataDownloadMapper;
    }

    @Transactional
    public Mono<Void> insertContractInfo(List<ContractInfoDTO> contractInfos) {
        return Flux.fromIterable(contractInfos)
                .buffer(2000) // 2000개씩 배치 처리
                .flatMap(batch -> Mono.fromRunnable(() -> dataDownloadMapper.insertContractInfoBatch(batch)))
                .then();
    }

    @Transactional
    public Mono<Void> insertContractInfoDetails(List<ContractInfoDetailDTO> contractInfoDetails) {
        return Flux.fromIterable(contractInfoDetails)
                .buffer(2000) // 2000개씩 배치 처리
                .flatMap(batch -> Mono.fromRunnable(() -> dataDownloadMapper.insertContractInfoDetailBatch(batch)))
                .then();
    }

    @Transactional
    public Mono<Void> ContractInfoCnstwk(List<ContractInfoCnstwkDTO> contractInfoDetails) {
        return Flux.fromIterable(contractInfoDetails)
                .buffer(2000) // 2000개씩 배치 처리
                .flatMap(batch -> Mono.fromRunnable(() -> dataDownloadMapper.insertContractInfoCnstwkBatch(batch)))
                .then();
    }

    @Transactional
    public Mono<Void> ContractInfoServc(List<ContractInfoServcDTO> contractInfoDetails) {
        return Flux.fromIterable(contractInfoDetails)
                .buffer(2000) // 2000개씩 배치 처리
                .flatMap(batch -> Mono.fromRunnable(() -> dataDownloadMapper.insertContractInfoServcBatch(batch)))
                .then();
    }
}
