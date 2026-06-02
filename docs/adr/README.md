# ADR (Architecture Decision Records)

이 디렉토리는 **중요한 아키텍처/DB/데이터 파이프라인/권한/배포 결정**을 ADR 형태로 기록합니다.
코드 변경의 “결정(why)”을 남겨, 시간이 지나도 맥락을 복구할 수 있게 합니다.

## ADR 작성 규칙

- **파일명**: `ADR-0001-<kebab-case-title>.md`
- **번호**: 연속 증가 (0001, 0002, ...)
- **상태(Status)**: `Proposed` / `Accepted` / `Deprecated` / `Superseded`
- **PR 연결**: ADR을 추가/수정한 PR 본문에 ADR 링크를 첨부합니다.

## 새 ADR 작성 방법

1. 템플릿 복사: `ADR-TEMPLATE.md` → `ADR-000X-...md`
2. 내용 작성 후 `Status`를 먼저 채웁니다.
3. 아래 목록에 링크를 추가합니다.

## ADR 목록

- [ADR-0001: SpecificItem_Raw_Ingestion_Job](ADR-0001-specific-item-raw-ingestion-job.md) — **Accepted** (2026-06-02)

