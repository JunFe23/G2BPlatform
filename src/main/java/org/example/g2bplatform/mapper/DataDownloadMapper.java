package org.example.g2bplatform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.g2bplatform.DTO.ContractInfoDTO;
import org.example.g2bplatform.DTO.ContractInfoDetailDTO;
import org.example.g2bplatform.model.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataDownloadMapper {
    void insertContractInfoBatch(@Param("contractInfos") List<ContractInfoDTO> contractInfos);
    void insertContractInfoDetailBatch(@Param("contractInfoDetails") List<ContractInfoDetailDTO> contractInfoDetails);
    void insertContractInfoChangeHistoryBatch(@Param("contractInfoChangeHistories") List<ContractInfoChangeHistory> contractInfoChangeHistories);
    void insertContractInfoPPSSrchBatch(@Param("contractInfoPPSSrches") List<ContractInfoPPSSrch> contractInfoPPSSrches);
    void insertContractInfoCnstwkBatch(@Param("contractInfoCnstwks") List<ContractInfoCnstwk> contractInfoCnstwks);
    void insertContractInfoServcBatch(@Param("contractInfoServcs") List<ContractInfoServc> contractInfoServcs);
}
