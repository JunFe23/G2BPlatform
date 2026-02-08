# 인증(Auth) 환경변수

## JWT

- `JWT_SECRET`: JWT 서명 키 (32자 이상 권장). 미설정 시 기본값 사용.
- `JWT_EXPIRES_IN_MS`: 토큰 만료 시간(ms). 기본 86400000(24시간).

## 앱 환경

- `APP_ENV`: `dev` | `prod`
    - `dev`: 비밀번호 재설정 요청 시 `devResetToken`을 API 응답에 포함 (테스트용)
    - `prod`: 토큰 미노출 (이메일 연동 예정)

## application.properties 기본값

```properties
jwt.secret=${JWT_SECRET:g2b-platform-default-secret-key-change-in-production-min32chars}
jwt.expires-in-ms=${JWT_EXPIRES_IN_MS:86400000}
app.env=${APP_ENV:dev}
```

## 운영 환경 권장

- `APP_ENV=prod`
- `JWT_SECRET`: 32자 이상 무작위 문자열
- `spring.jpa.hibernate.ddl-auto=validate` 또는 Flyway 등 마이그레이션 사용
