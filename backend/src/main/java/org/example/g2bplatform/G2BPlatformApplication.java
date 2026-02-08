package org.example.g2bplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@MapperScan("org.example.g2bplatform.mapper")
@EnableJpaAuditing
public class G2BPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(G2BPlatformApplication.class, args);
    }

}
