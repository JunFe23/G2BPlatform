package org.example.g2bplatform.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "main";
    }

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final WebClient webClient;

    public HomeController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://apis.data.go.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostMapping("/fetch")
    public String fetch(Model model) {
        // 여기서 필요한 로직을 수행합니다.
        model.addAttribute("message", "데이터를 가져왔습니다!");
        return "main";
    }

    @GetMapping("/download")
    public Mono<ResponseEntity<String>> fetchDataFromApi() {
        String endpoint = "/1230000/CntrctInfoService01/getCntrctInfoListThng01";
        // 이미 인코딩된 serviceKey
        String serviceKey = "Ayf0jexFhEl7e9BLvEjDXdBqUYUoJ7ekILYytvJ2dUy0f%2Fd6xKClhtcFIyjB1GtffyOBHLKhbRIx6f7bw9L6VA%3D%3D";

        // 인코딩된 serviceKey를 사용하여 URI를 직접 생성
        String urlString = "https://apis.data.go.kr" + endpoint +
                "?serviceKey=" + serviceKey +
                "&numOfRows=10" +
                "&pageNo=1" +
                "&inqryDiv=1" +
                "&inqryBgnDt=201701010000" +
                "&inqryEndDt=201701012359" +
                "&type=json";

        // URI 생성 및 로깅
        URI uri = URI.create(urlString);
        logger.info("Generated URI: {}", uri);

        // WebClient를 사용하여 API 요청 보내기
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("Received response: {}", response)) // 응답 로깅
                .map(response -> ResponseEntity.ok().body(response))
                .onErrorResume(e -> {
                    logger.error("Error while fetching data: ", e);
                    return Mono.just(ResponseEntity.status(500).body("{\"error\":\"Error occurred while fetching data.\"}"));
                });
    }
}
