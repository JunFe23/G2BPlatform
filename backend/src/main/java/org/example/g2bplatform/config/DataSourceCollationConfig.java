package org.example.g2bplatform.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * DB 스키마는 전 테이블 utf8mb4_unicode_ci 이지만, JDBC 커넥션 기본 collation 은
 * latin1_swedish_ci 로 잡힌다. 통합 UNION 조회(탑 수주 현황: specific_item ∪ market_contract ∪
 * top_manual_contract)처럼 한 컬럼이 어떤 브랜치에선 리터럴, 다른 브랜치에선 실제 컬럼이면
 * 'Illegal mix of collations for operation UNION'(MySQL 1271) 이 발생한다.
 * 커넥션 세션 collation 을 스키마와 동일하게 맞춰 근본 원인을 제거한다.
 * (character_set_client/results 는 건드리지 않으므로 데이터 인코딩 동작은 불변.)
 */
@Configuration
public class DataSourceCollationConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof HikariDataSource ds && ds.getConnectionInitSql() == null) {
            ds.setConnectionInitSql("SET collation_connection = utf8mb4_unicode_ci");
        }
        return bean;
    }
}
