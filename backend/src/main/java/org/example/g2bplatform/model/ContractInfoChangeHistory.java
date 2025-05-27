package org.example.g2bplatform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_info_list_thing_change_history")
@Data
public class ContractInfoChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 키, 자동 증가

    @Column(name = "untyCntrctNo", length = 35)
    private String untyCntrctNo; // 통합계약번호

    @Column(name = "bsnsDivNm", length = 30)
    private String bsnsDivNm; // 업무구분명

    @Column(name = "dcsnCntrctNo", length = 35)
    private String dcsnCntrctNo; // 확정계약번호

    @Column(name = "cntrctRefNo", length = 35)
    private String cntrctRefNo; // 계약참조번호

    @Column(name = "cntrctNm", length = 100)
    private String cntrctNm; // 계약명

    @Column(name = "cmmnCntrctYn", length = 1)
    private String cmmnCntrctYn; // 공동계약여부

    @Column(name = "lngtrmCtnuDivNm", length = 30)
    private String lngtrmCtnuDivNm; // 장기계속구분명

    @Column(name = "cntrctCnclsDate")
    @Temporal(TemporalType.DATE)
    private LocalDate cntrctCnclsDate; // 계약체결일자

    @Column(name = "cntrctPrd", length = 70)
    private String cntrctPrd; // 계약기간

    @Column(name = "baseLawNm", length = 1200)
    private String baseLawNm; // 근거법률명

    @Column(name = "totCntrctAmt", precision = 15, scale = 2)
    private BigDecimal totCntrctAmt; // 총계약금액

    @Column(name = "thtmCntrctAmt", precision = 15, scale = 2)
    private BigDecimal thtmCntrctAmt; // 금차계약금액

    @Column(name = "grntymnyRate", precision = 7, scale = 4)
    private BigDecimal grntymnyRate; // 보증금률

    @Column(name = "cntrctInfoUrl", length = 500)
    private String cntrctInfoUrl; // 계약정보URL

    @Column(name = "payDivNm", length = 30)
    private String payDivNm; // 지급구분명

    @Column(name = "reqNo", length = 70)
    private String reqNo; // 요청번호

    @Column(name = "ntceNo", length = 40)
    private String ntceNo; // 공고번호

    @Column(name = "cntrctInsttCd", length = 7)
    private String cntrctInsttCd; // 계약기관코드

    @Column(name = "cntrctInsttNm", length = 200)
    private String cntrctInsttNm; // 계약기관명

    @Column(name = "cntrctInsttJrsdctnDivNm", length = 200)
    private String cntrctInsttJrsdctnDivNm; // 계약기관소관구분명

    @Column(name = "cntrctInsttChrgDeptNm", length = 100)
    private String cntrctInsttChrgDeptNm; // 계약기관담당부서명

    @Column(name = "cntrctInsttOfclNm", length = 100)
    private String cntrctInsttOfclNm; // 계약기관담당자명

    @Column(name = "cntrctInsttOfclTelNo", length = 25)
    private String cntrctInsttOfclTelNo; // 계약기관담당자전화번호

    @Column(name = "cntrctInsttOfclFaxNo", length = 25)
    private String cntrctInsttOfclFaxNo; // 계약기관담당자팩스번호

    @Column(name = "dminsttList", length = 2000)
    private String dminsttList; // 수요기관목록

    @Column(name = "corpList", length = 4000)
    private String corpList; // 업체목록

    @Column(name = "cntrctDtlInfoUrl", length = 500)
    private String cntrctDtlInfoUrl; // 계약상세정보URL

    @Column(name = "crdtrNm", length = 200)
    private String crdtrNm; // 채권자명

    @Column(name = "baseDtls", length = 1000)
    private String baseDtls; // 근거내역

    @Column(name = "cntrctCnclsMthdNm", length = 30)
    private String cntrctCnclsMthdNm; // 계약체결방법명

    @Column(name = "rgstDt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt; // 등록일시

    @Column(name = "chgDt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt; // 변경일시

    @Column(name = "dfrcmpnstRt", precision = 5, scale = 2)
    private BigDecimal dfrcmpnstRt; // 지체상금율

    @Column(name = "linkInsttNm", length = 200)
    private String linkInsttNm; // 연계기관명

    @Column(name = "d2bMngCntrctSttusNm", length = 100)
    private String d2bMngCntrctSttusNm; // 방사청관리계약상태명

    @Column(name = "d2bMngPrearngAmt", precision = 15, scale = 2)
    private BigDecimal d2bMngPrearngAmt; // 방사청관리예정금액

    @Column(name = "d2bMngBidMthdNm", length = 100)
    private String d2bMngBidMthdNm; // 방사청관리입찰방법명

    @Column(name = "d2bMngDcsnNo", length = 10)
    private String d2bMngDcsnNo; // 방사청관리판단번호

    @Column(name = "infoBizYn", length = 1)
    private String infoBizYn; // 정보화사업여부
}
