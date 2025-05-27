package org.example.g2bplatform.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ContractInfoDetailDTO {

    private Long id; // 기본 키

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date cntrctCnclsDate; // 계약 체결 일자

    private String untyCntrctNo; // 통합 계약번호
    private String dcsnCntrctNo; // 확정 계약번호
    private String cntrctRefNo; // 계약 참조번호
    private String prdctClsfcNo; // 물품 분류번호
    private String prdctIdntNo; // 물품 식별번호
    private String prdctClsfcNoNm; // 품명 (물품분류번호 8자리 한글명)
    private String krnPrdctNm; // 한글 품목명
    private String orgplceCd; // 원산지 코드
    private String orgplceNm; // 원산지명
    private BigDecimal qtyUprcAmt; // 수량 단가 금액 (원화)
    private BigDecimal prdctQty; // 물품 수량
    private BigDecimal prdctAmt; // 물품 금액 (원화)
    private String dlvryCndtnCd; // 인도 조건 코드
    private String dlvryCndtnNm; // 인도 조건명
    private Integer dlvrDaynum; // 납품일 수
    private String dlvrTmlmt; // 납품 기한

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rgstDt; // 등록 일시 (YYYY-MM-DD HH:MM:SS)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime chgDt; // 변경 일시 (YYYY-MM-DD HH:MM:SS)
    private String yyyymm;
}
