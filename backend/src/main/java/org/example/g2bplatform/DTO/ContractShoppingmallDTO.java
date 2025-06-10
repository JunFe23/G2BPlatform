package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ContractShoppingmallDTO {

    private String shopngCntrctNo;
    private String shopngCntrctSno;
    private String prdctImgUrl;
    private String cntrctCorpNm;
    private String entrprsDivNm;
    private String drctPrdctnCnfrmCertHoldCorpYn;
    private String entrprsDivInfrmtn;
    private String cntrctMthdNm;
    private String prdctSpecNm;
    private String exclncPrcrmntPrdctYn;
    private String bndeFlawGrntyPrdctYn;
    private String smetprCmptProdctYn;
    private BigDecimal cntrctPrceAmt;
    private BigDecimal orderCalclPrceAmt;
    private BigDecimal dscntAmt;
    private String prdctUnit;
    private String prdctOrgplceNm;
    private String orgplceMainCmpnt1Cntnts;
    private String orgplceMainCmpnt1Nm;
    private String orgplceMainCmpnt2Cntnts;
    private String orgplceMainCmpnt2Nm;
    private String orgplceCoreCmpntCntnts;
    private String orgplceCoreCmpntNm;
    private String assmblCntryMainCmpnt1Cntnts;
    private String assmblCntryMainCmpnt1Nm;
    private String assmblCntryMainCmpnt2Cntnts;
    private String assmblCntryMainCmpnt2Nm;
    private String assmblCntryCoreCmpntCntnts;
    private String assmblCntryCoreCmpntNm;
    private String prdctMakrNm;
    private String prdctDlvrPlceNm;
    private String prdctDlvryCndtnNm;
    private String prdctSplyRgnNm;
    private String vatAplDivNm;
    private String dlvrTmlmtDaynum;
    private String dlvrTmlmtDscrpt;
    private String prcrmntFeeInclsnCntnts;
    private String dscntBgnDate;
    private String dscntEndDate;
    private String remndrQty;
    private String cntrctDeptNm;
    private String cntrctDeptTelNo;
    private String cntrctOfclNm;
    private String cntrctOfclEmail;
    private String splyTmlmtCntnts;
    private String acptncInsttNm;
    private String inspctInsttNm;
    private String cntrctSpcmntMtr;
    private String prodctChrInfoIntrcn;
    private String prodctCertList;
    private String cntrctCorpNo;
    private String specDocAtchFileNm1;
    private String specDocAtchFileNm2;
    private String specDocAtchFileNm3;
    private String specDocAtchFileNm4;
    private String specDocAtchFileNm5;
    private String specDocAtchFileNmUrl1;
    private String specDocAtchFileNmUrl2;
    private String specDocAtchFileNmUrl3;
    private String specDocAtchFileNmUrl4;
    private String specDocAtchFileNmUrl5;
    private String prdctLrgclsfcCd;
    private String prdctLrgclsfcNm;
    private String prdctMidclsfcCd;
    private String prdctMidclsfcNm;
    private String prdctClsfcNo;
    private String prdctClsfcNoNm;
    private String dtilPrdctClsfcNo;
    private String prdctIdntNo;
    private String levDivNm;
    private String qltyRltnCertInfo;
    private String avrgDlvyTime;
    private String prefrpurchsObjCertNm;
    private String dutyPurchsObjCertNm;
    private String qltyMngmtPrgnCorpYn;
    private String hdoffceLocplc;
    private String fctryLocplc;
    private String refeMtr;
    private String cntrctDate;
    private String splyJrsdctRgnNm;
    private String cntrctBgnDate;
    private String cntrctEndDate;
    private String splyCorpChrgDeptNm;
    private String splyCorpChrgDeptTelNo;
    private String publicProcurementOfficeInfrmtn;
    private String prdctDtlInfo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt;
}

