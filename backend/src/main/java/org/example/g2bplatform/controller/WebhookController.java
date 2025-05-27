package org.example.g2bplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
public class WebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<String> deployProject() {
        try {
            // 1. git pull
            ProcessBuilder pull = new ProcessBuilder("git", "pull");
            pull.directory(new File("/Users/junfe/Desktop/G2B/G2BPlatform")); // ✅ 여기를 실제 경로로!
            pull.inheritIO().start().waitFor();

            // 2. gradle build
            ProcessBuilder build = new ProcessBuilder("./gradlew", "bootJar");
            build.directory(new File("/Users/junfe/Desktop/G2B/G2BPlatform"));
            build.inheritIO().start().waitFor();

            // 3. 기존 실행중인 Spring Boot kill
            Runtime.getRuntime().exec("pkill -f 'java -jar'");

            // 4. 새 jar 실행
            Runtime.getRuntime().exec("nohup java -jar /Users/junfe/Desktop/G2B/G2BPlatform/build/libs/g2bplatform-0.0.1-SNAPSHOT.jar &");

            return ResponseEntity.ok("✅ Auto deployment complete.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Deployment failed.");
        }
    }
}
