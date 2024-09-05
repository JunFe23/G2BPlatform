package org.example.g2bplatform.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "main";
    }

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final WebClient webClient;

    public HomeController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://apis.data.go.kr").build(); // 기본 URL 설정
    }

    @PostMapping("/fetch")
    public String fetch(Model model) {
        // 여기서 필요한 로직을 수행합니다.
        model.addAttribute("message", "데이터를 가져왔습니다!");
        return "main";
    }

    @GetMapping("/download")
    public Mono<String> fetchDataFromApi() {
        String endpoint = "/1230000/CntrctInfoService01/getCntrctInfoListThng01";
        String queryParams = "?serviceKey=Ayf0jexFhEl7e9BLvEjDXdBqUYUoJ7ekILYytvJ2dUy0f%2Fd6xKClhtcFIyjB1GtffyOBHLKhbRIx6f7bw9L6VA%3D%3D&numOfRows=10&pageNo=1&inqryDiv=1&inqryBgnDt=201701010000&inqryEndDt=201712312359&type=json";

        // WebClient를 사용하여 API 요청 보내기
        return webClient.get()
                .uri(endpoint + queryParams)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("Received response: {}", response)) // 응답 로깅
                .onErrorResume(e -> {
                    logger.error("Error while fetching data: ", e);
                    return Mono.just("Error occurred while fetching data.");
                });
    }
}
