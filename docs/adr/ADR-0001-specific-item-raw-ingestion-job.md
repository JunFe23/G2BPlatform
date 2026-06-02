# ADR-0001: SpecificItem_Raw_Ingestion_Job

- **Status**: Accepted
- **Date**: 2026-06-02
- **Owners**: G2BPlatform

## Context

조달데이터허브 보고서 통합 이후 “특정품목 조달 내역” CSV(대용량, 연도별)를 원천(raw)로 받아 적재한 뒤,
물품/쇼핑몰을 한 화면에서 통합 조회할 수 있도록 파이프라인을 재구성해야 했다.

기존 방식(요청-응답 동기 업로드)은 아래 운영/UX 문제가 있었다.

- 대용량 CSV 적재 중 브라우저가 멈춘 것처럼 보이며, 새로고침 시 상태가 사라짐
- 적재 진행률(%) / 남은시간(ETA)을 사용자에게 제공하기 어려움
- 업로드 재시도/중복/실패 원인 추적이 불명확함

## Decision

특정품목 조달 내역 CSV 적재를 **비동기 Job 방식**으로 운영한다.

- **업로드 요청**은 파일을 서버 임시 경로에 저장하고 **`jobId`를 즉시 반환**
- **서버**는 백그라운드에서 CSV를 파싱/배치 INSERT하며 진행률을 DB에 기록
- **클라이언트**는 `jobId`로 상태를 주기적으로 조회해 진행률/ETA를 표시 (새로고침 복구 포함)

## Consequences

### Positive

- 대용량 적재 UX 개선: 진행률/ETA 표기, 새로고침 복구
- 운영 추적 개선: Job 상태(Queued/Running/Success/Failed), 업로더 정보 기록
- 서버 처리 흐름이 명확해져 향후 취소/재시도 확장 용이

### Negative / Risks

- 진행률 기록 업데이트와 상태 조회로 DB 트래픽이 증가
- 서버 임시 파일(`/tmp`) 저장이 필요하며, 디스크 용량/정리 정책 필요
- 작업 중단(취소) 미구현 시 서버 재시작이 필요할 수 있음

### Operational notes

- 동시 적재는 executor 풀 크기로 제한
- 업로드/적재 로그는 `csv_upload_history`, 진행률은 `csv_upload_job`에서 확인

## Alternatives considered

- **동기 업로드 유지**: 구현은 단순하지만 대용량에서 UX/운영 한계가 큼
- **SSE/WebSocket 실시간 푸시**: 폴링 대비 효율적이나 초기 구현 복잡도 상승 (향후 개선 후보)
- **외부 큐(예: Redis/RabbitMQ) 워커**: 확장성은 좋지만 운영 구성 복잡도 증가

## Links

- Docs: `docs/plan_csv_upload_etl_migration.md`
- Source: `backend/src/main/java/org/example/g2bplatform/controller/CsvUploadController.java`

