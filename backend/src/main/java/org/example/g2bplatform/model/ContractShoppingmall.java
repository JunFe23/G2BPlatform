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
    private String drctPrdctnCnfrmCertHoldCorpYn; // 직접생산확인증명서 보유업체 여부

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
    private String orgplceMainCmpnt1Nm; // 주요부품1 원산지명

    @Column(length = 100)
    private String orgplceMainCmpnt2Cntnts; // 원산지 주요부품2내용

    @Column(length = 100)
    private String orgplceMainCmpnt2Nm; // 주요부품2 원산지명

}

