package org.example.g2bplatform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 서버에서는 스케줄러 실행, 로컬에서는 비활성화.
 * 로컬: application-local.properties 에 app.scheduling.enabled=false 또는 --spring.profiles.active=local
 */
@Configuration
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class SchedulingConfig {
}
