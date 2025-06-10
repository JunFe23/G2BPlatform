package org.example.g2bplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_shoppingmall")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractShoppingmall {

    @Id
    @Column(length = 11)
    private String shopngCntrctNo; // 쇼핑계약번호 (PK)

    @Column(length = 22)
    private String shopngCntrctSno; // 쇼핑계약순번

    @Column(length = 500)
    private String prdctImgUrl; // 상품이미지URL

    @Column(length = 100)
    private String cntrctCorpNm; // 계약업체명

    @Column(length = 100)
    private String entrprsDivNm; // 기업구분명

    @Column(length = 1)
    private String drctPrdctnCnfrmCertHoldCorpYn; // 직접생산확인증명서 보유여부

    @Column(length = 100)
    private String entrprsDivInfrmtn; // 기업구분안내

    @Column(length = 200)
    private String cntrctMthdNm; // 계약방법명

    @Column(length = 500)
    private String prdctSpecNm; // 물품규격명

    @Column(length = 1)
    private String exclncPrcrmntPrdctYn; // 우수조달물품여부

    @Column(length = 1)
    private String bndeFlawGrntyPrdctYn; // 일괄하자보증물품여부

    @Column(length = 1)
    private String smetprCmptProdctYn; // 중소기업자간 경쟁제품여부

    private BigDecimal cntrctPrceAmt; // 계약가격금액

    private BigDecimal orderCalclPrceAmt; // 주문산정가격금액

    private BigDecimal dscntAmt; // 할인금액

    @Column(length = 100)
    private String prdctUnit; // 물품단위

    @Column(length = 50)
    private String prdctOrgplceNm; // 물품원산지명

    @Column(length = 100)
    private String orgplceMainCmpnt1Cntnts; // 원산지 주요부품1내용

    @Column(length = 100)
    private String orgplceMainCmpnt1Nm; // 원산지 주요부품1명

    @Column(length = 100)
    private String orgplceMainCmpnt2Cntnts; // 원산지 주요부품2내용

    @Column(length = 100)
    private String orgplceMainCmpnt2Nm; // 원산지 주요부품2명

    @Column(length = 100)
    private String orgplceCoreCmpntCntnts; // 원산지 핵심부품내용

    @Column(length = 100)
    private String orgplceCoreCmpntNm; // 원산지 핵심부품명

    @Column(length = 100)
    private String assmblCntryMainCmpnt1Cntnts; // 조립국가 주요부품1 내용

    @Column(length = 100)
    private String assmblCntryMainCmpnt1Nm; // 조립국가 주요부품1 명

    @Column(length = 100)
    private String assmblCntryMainCmpnt2Cntnts; // 조립국가 주요부품2 내용

    @Column(length = 100)
    private String assmblCntryMainCmpnt2Nm; // 조립국가 주요부품2 명

    @Column(length = 100)
    private String assmblCntryCoreCmpntCntnts; // 조립국가 핵심부품 내용

    @Column(length = 100)
    private String assmblCntryCoreCmpntNm; // 조립국가 핵심부품 명

    @Column(length = 100)
    private String prdctMakrNm; // 물품제조사명

    @Column(length = 100)
    private String prdctDlvrPlceNm; // 물품납품장소명

    @Column(length = 100)
    private String prdctDlvryCndtnNm; // 물품인도조건명

    @Column(length = 100)
    private String prdctSplyRgnNm; // 물품공급지역명

    @Column(length = 100)
    private String vatAplDivNm; // 부가가치세적용구분명

    @Column(length = 100)
    private String dlvrTmlmtDaynum; // 납품기한일수

    @Column(length = 100)
    private String dlvrTmlmtDscrpt; // 납품기한설명

    @Column(length = 1000)
    private String prcrmntFeeInclsnCntnts; // 조달수수료포함내용

    @Column(length = 20)
    private String dscntBgnDate; // 할인시작일자

    @Column(length = 20)
    private String dscntEndDate; // 할인종료일자

    @Column(length = 50)
    private String remndrQty; // 잔여수량

    @Column(length = 500)
    private String cntrctDeptNm; // 계약부서명

    @Column(length = 100)
    private String cntrctDeptTelNo; // 계약부서 전화번호

    @Column(length = 100)
    private String cntrctOfclNm; // 계약담당자명

    @Column(length = 100)
    private String cntrctOfclEmail; // 계약담당자 이메일

    @Column(length = 1000)
    private String splyTmlmtCntnts; // 공급기한내용

    @Column(length = 100)
    private String acptncInsttNm; // 검수기관명

    @Column(length = 100)
    private String inspctInsttNm; // 검사기관명

    @Column(length = 100)
    private String cntrctSpcmntMtr; // 계약특기사항

    @Column(length = 1000)
    private String prodctChrInfoIntrcn; // 제품특성정보소개

    @Column(length = 1000)
    private String prodctCertList; // 제품인증목록

    @Column(length = 100)
    private String cntrctCorpNo; // 계약업체번호

    private LocalDateTime rgstDt; // 등록일시

    private LocalDateTime chgDt; // 변경일시
}

