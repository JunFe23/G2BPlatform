package org.example.g2bplatform.deploy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@RestController
@Slf4j
public class WebhookController {

    @PostMapping("/webhook")
    public String triggerDeployment() {
        try {
            ProcessBuilder builder = new ProcessBuilder("sh", "-c", "/app/deploy.sh");

            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                log.info(line);
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            log.info("Deployment finished with exit code {}", exitCode);

            return "✅ 배포 완료\n" + output;
        } catch (Exception e) {
            log.error("❌ 배포 실패", e);
            return "❌ 배포 실패: " + e.getMessage();
        }
    }
}