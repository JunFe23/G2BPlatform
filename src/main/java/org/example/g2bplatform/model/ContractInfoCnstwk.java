package org.example.g2bplatform.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_info_construction_work")
@Data
public class ContractInfoCnstwk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "unty_cntrct_no", nullable = false, length = 13)
    private String untyCntrctNo;

    @Column(name = "bsns_div_nm", nullable = false, length = 30)
    private String bsnsDivNm;

    @Column(name = "dcsn_cntrct_no", length = 35)
    private String dcsnCntrctNo;

    @Column(name = "cntrct_ref_no", length = 35)
    private String cntrctRefNo;

    @Column(name = "cnstwk_nm", length = 100)
    private String cnstwkNm;

    @Column(name = "cmmn_cntrct_yn", length = 1)
    private String cmmnCntrctYn;

    @Column(name = "lngtrm_ctnu_div_nm", length = 30)
    private String lngtrmCtnuDivNm;

    @Column(name = "cntrct_cncls_date")
    private LocalDate cntrctCnclsDate;

    @Column(name = "cntrct_prd", length = 70)
    private String cntrctPrd;

    @Column(name = "base_law_nm", length = 1200)
    private String baseLawNm;

    @Column(name = "tot_cntrct_amt", precision = 25, scale = 2)
    private BigDecimal totCntrctAmt;

    @Column(name = "thtm_cntrct_amt", precision = 25, scale = 2)
    private BigDecimal thtmCntrctAmt;

    @Column(name = "grntymny_rate", precision = 25, scale = 3)
    private BigDecimal grntymnyRate;

    @Column(name = "cntrct_info_url", length = 500)
    private String cntrctInfoUrl;

    @Column(name = "pay_div_nm", length = 30)
    private String payDivNm;

    @Column(name = "req_no", length = 70)
    private String reqNo;

    @Column(name = "ntce_no", length = 40)
    private String ntceNo;

    @Column(name = "cntrct_instt_cd", length = 7)
    private String cntrctInsttCd;

    @Column(name = "cntrct_instt_nm", length = 200)
    private String cntrctInsttNm;

    @Column(name = "cntrct_instt_jrsdctn_div_nm", length = 200)
    private String cntrctInsttJrsdctnDivNm;

    @Column(name = "cntrct_instt_chrg_dept_nm", length = 100)
    private String cntrctInsttChrgDeptNm;

    @Column(name = "cntrct_instt_ofcl_nm", length = 100)
    private String cntrctInsttOfclNm;

    @Column(name = "cntrct_instt_ofcl_tel_no", length = 25)
    private String cntrctInsttOfclTelNo;

    @Column(name = "cntrct_instt_ofcl_fax_no", length = 25)
    private String cntrctInsttOfclFaxNo;

    @Column(name = "dminstt_list", length = 2000)
    private String dminsttList;

    @Column(name = "corp_list", length = 4000)
    private String corpList;

    @Column(name = "cntrct_dtl_info_url", length = 500)
    private String cntrctDtlInfoUrl;

    @Column(name = "crdtr_nm", length = 200)
    private String crdtrNm;

    @Column(name = "base_dtls", length = 1000)
    private String baseDtls;

    @Column(name = "cntrct_cncls_mthd_nm", length = 30)
    private String cntrctCnclsMthdNm;

    @Column(name = "prces_change_apl_bss_cd", length = 30)
    private String prcesChangeAplBssCd;

    @Column(name = "prces_change_apl_bss_nm", length = 100)
    private String prcesChangeAplBssNm;

    @Column(name = "rgst_dt", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt;

    @Column(name = "chg_dt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt;

    @Column(name = "dfrcmpnst_rt", precision = 8, scale = 2)
    private BigDecimal dfrcmpnstRt;

    @Column(name = "cbgn_date")
    private LocalDate cbgnDate;

    @Column(name = "thtm_ccmplt_date")
    private LocalDate thtmCcmpltDate;

    @Column(name = "ttal_ccmplt_date")
    private LocalDate ttalCcmpltDate;

    @Column(name = "link_instt_nm", length = 200)
    private String linkInsttNm;

    @Column(name = "d2b_mng_cntrct_sttus_nm", length = 100)
    private String d2bMngCntrctSttusNm;

    @Column(name = "d2b_mng_prearng_amt", precision = 21, scale = 2)
    private BigDecimal d2bMngPrearngAmt;

    @Column(name = "d2b_mng_bid_mthd_nm", length = 100)
    private String d2bMngBidMthdNm;

    @Column(name = "d2b_mng_cnstwk_no", length = 10)
    private String d2bMngCnstwkNo;

    @JsonProperty("pubPrcrmntLrgClsfcNm")
    @Column(name = "pub_prcrmnt_lrgclsfc_nm", length = 100)
    private String pubPrcrmntLrgClsfcNm;

    @JsonProperty("pubPrcrmntMidClsfcNm")
    @Column(name = "pub_prcrmnt_midclsfc_nm", length = 100)
    private String pubPrcrmntMidClsfcNm;

    @Column(name = "pub_prcrmnt_clsfc_no", length = 10)
    private String pubPrcrmntClsfcNo;

    @JsonProperty("pubPrcrmntClsfcNm")
    @Column(name = "pub_prcrmnt_clsfc_nm", length = 100)
    private String pubPrcrmntClsfcNm;

    @Column(name = "cntrct_date")
    private LocalDate cntrctDate;

}