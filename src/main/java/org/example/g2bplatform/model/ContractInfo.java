package org.example.g2bplatform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_info_list_thing")
@Data
public class ContractInfo {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Transient
    private String resultCode;

    @Transient
    private String resultMsg;

    @Transient
    private int numOfRows;

    @Transient
    private int pageNo;

    @Transient
    private int totalCount;

    @Id
    @Column(name = "untyCntrctNo", length = 13, nullable = false)
    private String untyCntrctNo;

    @Column(name = "bsnsDivNm", length = 30, nullable = false)
    private String bsnsDivNm;

    @Column(name = "dcsnCntrctNo", length = 35)
    private String dcsnCntrctNo;

    @Column(name = "cntrctRefNo", length = 35)
    private String cntrctRefNo;

    @Column(name = "cntrctNm", length = 100)
    private String cntrctNm;

    @Column(name = "cmmnCntrctYn", length = 1)
    private String cmmnCntrctYn;

    @Column(name = "lngtrmCtnuDivNm", length = 30)
    private String lngtrmCtnuDivNm;

    @Column(name = "cntrctCnclsDate")
    @JsonProperty("cntrctCnclsDate") // JSON 필드명이 다를 경우 이 부분을 맞추어야 함
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cntrctCnclsDate;

    @Column(name = "cntrctPrd", length = 70)
    private String cntrctPrd;

    @Column(name = "baseLawNm", length = 1200)
    private String baseLawNm;

    @Column(name = "totCntrctAmt", precision = 15, scale = 2)
    private BigDecimal totCntrctAmt;

    @Column(name = "thtmCntrctAmt", precision = 15, scale = 2)
    private BigDecimal thtmCntrctAmt;

    @Column(name = "grntymnyRate", precision = 5, scale = 2)
    private BigDecimal grntymnyRate;

    @Column(name = "cntrctInfoUrl", length = 500)
    private String cntrctInfoUrl;

    @Column(name = "payDivNm", length = 30)
    private String payDivNm;

    @Column(name = "reqNo", length = 70)
    private String reqNo;

    @Column(name = "ntceNo", length = 40)
    private String ntceNo;

    @Column(name = "cntrctInsttCd", length = 7)
    private String cntrctInsttCd;

    @Column(name = "cntrctInsttNm", length = 200)
    private String cntrctInsttNm;

    @Column(name = "cntrctInsttJrsdctnDivNm", length = 200)
    private String cntrctInsttJrsdctnDivNm;

    @Column(name = "cntrctInsttChrgDeptNm", length = 100)
    private String cntrctInsttChrgDeptNm;

    @Column(name = "cntrctInsttOfclNm", length = 100)
    private String cntrctInsttOfclNm;

    @Column(name = "cntrctInsttOfclTelNo", length = 25)
    private String cntrctInsttOfclTelNo;

    @Column(name = "cntrctInsttOfclFaxNo", length = 25)
    private String cntrctInsttOfclFaxNo;

    @Column(name = "dminsttList", length = 2000)
    private String dminsttList;

    @Column(name = "corpList", length = 4000)
    private String corpList;

    @Column(name = "cntrctDtlInfoUrl", length = 500)
    private String cntrctDtlInfoUrl;

    @Column(name = "crdtrNm", length = 200)
    private String crdtrNm;

    @Column(name = "baseDtls", length = 1000)
    private String baseDtls;

    @Column(name = "cntrctCnclsMthdNm", length = 30)
    private String cntrctCnclsMthdNm;

    @Column(name = "rgstDt")
    @JsonProperty("rgstDt")  // JSON 필드명이 다를 경우 이 부분을 맞추어야 함
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt;

    @Column(name = "chgDt")
    @JsonProperty("chgDt")  // JSON 필드명이 다를 경우 이 부분을 맞추어야 함
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt;

    @Column(name = "dfrcmpnstRt", precision = 8, scale = 2)
    private BigDecimal dfrcmpnstRt;

    @Column(name = "linkInsttNm", length = 200)
    private String linkInsttNm;

    @Column(name = "d2bMngCntrctSttusNm", length = 100)
    private String d2bMngCntrctSttusNm;

    @Column(name = "d2bMngPrearngAmt", precision = 21, scale = 2)
    private BigDecimal d2bMngPrearngAmt;

    @Column(name = "d2bMngBidMthdNm", length = 100)
    private String d2bMngBidMthdNm;

    @Column(name = "d2bMngDcsnNo", length = 10)
    private String d2bMngDcsnNo;

    @Column(name = "pubPrcrmntLrgClsfcNm", length = 100)
    private String pubPrcrmntLrgClsfcNm;

    @Column(name = "pubPrcrmntMidClsfcNm", length = 100)
    private String pubPrcrmntMidClsfcNm;

    @Column(name = "pubPrcrmntClsfcNo", length = 10)
    private String pubPrcrmntClsfcNo;

    @Column(name = "pubPrcrmntClsfcNm", length = 100)
    private String pubPrcrmntClsfcNm;

    @Column(name = "cntrctDate")
    private LocalDate cntrctDate;

    @Column(name = "infoBizYn", length = 1)
    private String infoBizYn;
}