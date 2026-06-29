package org.example.g2bplatform.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 탑 수주 현황 민수(직접입력) 데이터 — MyBatis 매핑용 POJO (top_manual_contract).
 * 관급(specific_item/market_contract)과 달리 raw/ETL 없이 이 1건이 곧 최종 1행.
 */
public class TopManualContract {

    private Long id;
    private String type;                  // 물품|쇼핑몰|공사|용역
    private String vendorBizRegNo;        // 1188117437 | 1188119624
    private String vendorName;
    private String contractName;
    private String demandAgencyName;
    private String demandAgencyRegion;
    private String midCategory;           // 중분류(자유 텍스트)
    private String productClassification; // 품명내용/소분류(자유 텍스트)
    private String contractMethod;
    private String bidNoticeNo;
    private LocalDate firstContractDate;
    private Long firstContractAmount;
    private LocalDate lastContractDate;
    private Long lastContractAmount;
    private Integer contractChangeSeq;
    private String dataOrigin;            // 고정: 민수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVendorBizRegNo() { return vendorBizRegNo; }
    public void setVendorBizRegNo(String vendorBizRegNo) { this.vendorBizRegNo = vendorBizRegNo; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }

    public String getDemandAgencyName() { return demandAgencyName; }
    public void setDemandAgencyName(String demandAgencyName) { this.demandAgencyName = demandAgencyName; }

    public String getDemandAgencyRegion() { return demandAgencyRegion; }
    public void setDemandAgencyRegion(String demandAgencyRegion) { this.demandAgencyRegion = demandAgencyRegion; }

    public String getMidCategory() { return midCategory; }
    public void setMidCategory(String midCategory) { this.midCategory = midCategory; }

    public String getProductClassification() { return productClassification; }
    public void setProductClassification(String productClassification) { this.productClassification = productClassification; }

    public String getContractMethod() { return contractMethod; }
    public void setContractMethod(String contractMethod) { this.contractMethod = contractMethod; }

    public String getBidNoticeNo() { return bidNoticeNo; }
    public void setBidNoticeNo(String bidNoticeNo) { this.bidNoticeNo = bidNoticeNo; }

    public LocalDate getFirstContractDate() { return firstContractDate; }
    public void setFirstContractDate(LocalDate firstContractDate) { this.firstContractDate = firstContractDate; }

    public Long getFirstContractAmount() { return firstContractAmount; }
    public void setFirstContractAmount(Long firstContractAmount) { this.firstContractAmount = firstContractAmount; }

    public LocalDate getLastContractDate() { return lastContractDate; }
    public void setLastContractDate(LocalDate lastContractDate) { this.lastContractDate = lastContractDate; }

    public Long getLastContractAmount() { return lastContractAmount; }
    public void setLastContractAmount(Long lastContractAmount) { this.lastContractAmount = lastContractAmount; }

    public Integer getContractChangeSeq() { return contractChangeSeq; }
    public void setContractChangeSeq(Integer contractChangeSeq) { this.contractChangeSeq = contractChangeSeq; }

    public String getDataOrigin() { return dataOrigin; }
    public void setDataOrigin(String dataOrigin) { this.dataOrigin = dataOrigin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
