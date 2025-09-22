package org.example.g2bplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_shoppingmall",
        indexes = {
                @Index(name = "idx_prdct_clsfc_no", columnList = "prdctClsfcNo"),
                @Index(name = "idx_dtil_prdct_clsfc_no", columnList = "dtilPrdctClsfcNo"),
                @Index(name = "idx_prdct_idnt_no", columnList = "prdctIdntNo"),
                @Index(name = "idx_cntrct_date", columnList = "cntrctDate")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ContractShoppingmall {

    /** 복합키: 쇼핑계약번호 + 쇼핑계약순번 */
    @EmbeddedId
    private ContractShoppingmallId id;

    /** 상품이미지URL (선택) */
    @Column(length = 500)
    private String prdctImgUrl;

    /** 계약업체명 (필수) */
    @Column(length = 100, nullable = false)
    private String cntrctCorpNm;

    /** 기업구분명 (선택) */
    @Column(length = 100)
    private String entrprsDivNm;

    /** 계약방법명 (필수) */
    @Column(length = 100, nullable = false)
    private String cntrctMthdNm;

    /** 우수조달물품여부 (Y/N, 선택) */
    @Column(length = 1)
    private String exclncPrcrmntPrdctYn;

    /** 다수공급경쟁자여부 (Y/N, 선택) */
    @Column(length = 1)
    private String masYn;

    /** 중소기업자간경쟁제품여부 (Y/N, 필수) */
    @Column(length = 1, nullable = false)
    private String smetprCmptProdctYn;

    /** 계약가격금액 (정수 원화, 선택) */
    @Column(precision = 30, scale = 0)
    private BigDecimal cntrctPrceAmt;

    /** 물품단위 (선택) */
    @Column(length = 100)
    private String prdctUnit;

    /** 물품제조사명 (선택, 스펙 200) */
    @Column(length = 200)
    private String prdctMakrNm;

    /** 물품납품장소명 (선택, 스펙 500) */
    @Column(length = 500)
    private String prdctDlvrPlceNm;

    /** 물품인도조건명 (선택, 스펙 200) */
    @Column(length = 200)
    private String prdctDlvryCndtnNm;

    /** 물품공급지역명 (선택, 스펙 200) */
    @Column(length = 200)
    private String prdctSplyRgnNm;

    /** 납품기한일수 (선택, 스펙 10) */
    @Column(length = 10)
    private String dlvrTmlmtDaynum;

    /** 물품대분류코드 (선택, 2) */
    @Column(length = 2)
    private String prdctLrgclsfcCd;

    /** 물품대분류명 (선택, 100) */
    @Column(length = 100)
    private String prdctLrgclsfcNm;

    /** 물품중분류코드 (선택, 4) */
    @Column(length = 4)
    private String prdctMidclsfcCd;

    /** 물품중분류명 (선택, 100) */
    @Column(length = 100)
    private String prdctMidclsfcNm;

    /** 물품분류번호 (필수, 8) */
    @Column(length = 8, nullable = false)
    private String prdctClsfcNo;

    /** 품명 (필수, 200) */
    @Column(length = 200, nullable = false)
    private String prdctClsfcNoNm;

    /** 세부품명번호 (필수, 10) */
    @Column(length = 10, nullable = false)
    private String dtilPrdctClsfcNo;

    /** 세부품명 (필수, 200) */
    @Column(length = 200, nullable = false)
    private String dtilPrdctClsfcNoNm;

    /** 물품식별번호 (필수, 8) */
    @Column(length = 8, nullable = false)
    private String prdctIdntNo;

    /** 물품규격명 (선택, 500) */
    @Column(length = 500)
    private String prdctSpecNm;

    /** 계약일자 (선택, 'YYYY-MM-DD') */
    private LocalDate cntrctDate;

    /** 계약시작일자 (선택) */
    private LocalDate cntrctBgnDate;

    /** 계약종료일자 (선택) */
    private LocalDate cntrctEndDate;

    /** 계약부서명 (선택, 100) */
    @Column(length = 100)
    private String cntrctDeptNm;

    /** 제품인증목록 (선택, 4000) */
    @Column(length = 4000)
    private String prodctCertList;

    /** 등록일시 (필수) */
    @Column(nullable = false)
    private LocalDateTime rgstDt;

    /** 계약업체 사업자등록번호 (선택, 10) */
    @Column(length = 10)
    private String cntrctCorpBizno;

    /* ===== EmbeddedId 클래스 ===== */
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    public static class ContractShoppingmallId implements Serializable {
        /** 쇼핑계약번호 (필수, 20) */
        @Column(length = 20, nullable = false)
        private String shopngCntrctNo;

        /** 쇼핑계약순번 (필수, 22) */
        @Column(length = 22, nullable = false)
        private String shopngCntrctSno;
    }
}