package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ContractInfoDTO {

    private Long id;

    private String untyCntrctNo;
    private String bsnsDivNm;
    private String dcsnCntrctNo;
    private String cntrctRefNo;
    private String cntrctNm;
    private String cmmnCntrctYn;
    private String lngtrmCtnuDivNm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cntrctCnclsDate;

    private String cntrctPrd;
    private String baseLawNm;
    private BigDecimal totCntrctAmt;
    private BigDecimal thtmCntrctAmt;
    private BigDecimal grntymnyRate;
    private String cntrctInfoUrl;
    private String payDivNm;
    private String reqNo;
    private String ntceNo;
    private String cntrctInsttCd;
    private String cntrctInsttNm;
    private String cntrctInsttJrsdctnDivNm;
    private String cntrctInsttChrgDeptNm;
    private String cntrctInsttOfclNm;
    private String cntrctInsttOfclTelNo;
    private String cntrctInsttOfclFaxNo;
    private String dminsttList;
    private String corpList;
    private String cntrctDtlInfoUrl;
    private String crdtrNm;
    private String baseDtls;
    private String cntrctCnclsMthdNm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt;

    private BigDecimal dfrcmpnstRt;
    private String linkInsttNm;
    private String d2bMngCntrctSttusNm;
    private BigDecimal d2bMngPrearngAmt;
    private String d2bMngBidMthdNm;
    private String d2bMngDcsnNo;
    private String pubPrcrmntLrgClsfcNm;
    private String pubPrcrmntMidClsfcNm;
    private String pubPrcrmntClsfcNo;
    private String pubPrcrmntClsfcNm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cntrctDate;

    private String infoBizYn;
    private String yyyymm;
}
