package org.example.g2bplatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
@MapperScan("org.example.g2bplatform.mapper")
public class G2BPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(G2BPlatformApplication.class, args);
    }

}
