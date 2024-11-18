package org.example.g2bplatform.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "contract_info_detail_list_thing")
@Data
public class ContractInfoDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키, 자동 증가

    @Column(name = "cntrct_cncls_date")
    @Temporal(TemporalType.DATE)
    private Date cntrctCnclsDate; // 계약 체결 일자

    @Column(name = "unty_cntrct_no", length = 13, nullable = false)
    private String untyCntrctNo; // 통합 계약번호

    @Column(name = "dcsn_cntrct_no", length = 35)
    private String dcsnCntrctNo; // 확정 계약번호

    @Column(name = "cntrct_ref_no", length = 35)
    private String cntrctRefNo; // 계약 참조번호

    @Column(name = "prdct_clsfc_no", length = 8)
    private String prdctClsfcNo; // 물품 분류번호

    @Column(name = "prdct_idnt_no", length = 8)
    private String prdctIdntNo; // 물품 식별번호

    @Column(name = "prdct_clsfc_no_nm", length = 200)
    private String prdctClsfcNoNm; // 품명 (물품분류번호 8자리 한글명)

    @Column(name = "krn_prdct_nm", length = 200)
    private String krnPrdctNm; // 한글 품목명

    @Column(name = "orgplce_cd", length = 3)
    private String orgplceCd; // 원산지 코드

    @Column(name = "orgplce_nm", length = 200)
    private String orgplceNm; // 원산지명

    @Column(name = "qty_uprc_amt", precision = 25, scale = 2)
    private BigDecimal qtyUprcAmt; // 수량 단가 금액 (원화)

    @Column(name = "prdct_qty", precision = 25, scale = 2)
    private BigDecimal prdctQty; // 물품 수량

    @Column(name = "prdct_amt", precision = 25, scale = 2)
    private BigDecimal prdctAmt; // 물품 금액 (원화)

    @Column(name = "dlvry_cndtn_cd", length = 3)
    private String dlvryCndtnCd; // 인도 조건 코드

    @Column(name = "dlvry_cndtn_nm", length = 200)
    private String dlvryCndtnNm; // 인도 조건명

    @Column(name = "dlvr_daynum")
    private Integer dlvrDaynum; // 납품일 수

    @Column(name = "dlvr_tmlmt", length = 8)
    private String dlvrTmlmt; // 납품 기한

    @Column(name = "rgst_dt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt; // 등록 일시 (YYYY-MM-DD HH:MM:SS)

    @Column(name = "chg_dt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt; // 변경 일시 (YYYY-MM-DD HH:MM:SS)

}