package org.example.g2bplatform.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.g2bplatform.Model.ContractInfo;
import org.example.g2bplatform.Repostiory.ContractInfoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "main";
    }

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final WebClient webClient;
    private final ContractInfoRepository contractInfoRepository;
    private final ObjectMapper objectMapper;

    // 생성자에서 ObjectMapper를 주입받도록 수정
    public HomeController(WebClient.Builder webClientBuilder, ContractInfoRepository contractInfoRepository, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://apis.data.go.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.contractInfoRepository = contractInfoRepository;
        this.objectMapper = objectMapper; // 주입받은 ObjectMapper 사용
    }

    @PostMapping("/fetch")
    public Mono<ResponseEntity<String>> fetchDataFromApi(@RequestBody Map<String, String> requestData) {
        // 클라이언트로부터 받은 엔드포인트와 서비스 키
        String endpoint = requestData.get("endpoint");
        String serviceKey = requestData.get("serviceKey");
        String inqryBgnDt = requestData.get("inqryBgnDt");
        String inqryEndDt = requestData.get("inqryEndDt");

        // 첫 번째 요청을 통해 totalCount를 확인
        String initialUrl = buildUrl(endpoint, serviceKey, inqryBgnDt, inqryEndDt, 1);
        logger.info("Initial URI: {}", initialUrl);

        // WebClient를 사용하여 API 요청 보내기
        return webClient.get()
                .uri(URI.create(initialUrl))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("Received response: {}", response)) // 응답 로깅
                .flatMap(initialResponse -> {
                    try {
                        // JSON 응답을 파싱하여 resultCode와 resultMsg를 확인
                        JsonNode initialJsonNode = objectMapper.readTree(initialResponse);
                        JsonNode headerNode = initialJsonNode.path("response").path("header");
                        String resultCode = headerNode.path("resultCode").asText();
                        String resultMsg = headerNode.path("resultMsg").asText();
                        int totalCount = initialJsonNode.path("response").path("body").path("totalCount").asInt();
                        int numOfRows = 100; // 최대 100개씩 가져오기
                        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);

                        logger.info("Total Count: {}, Total Pages: {}", totalCount, totalPages);

                        // resultCode가 "00"이면 성공, 아니면 오류 메시지를 포함하여 반환
                        if ("00".equals(resultCode)) {
                            // 각 페이지에 대한 요청을 만들어 Flux로 처리
                            List<Mono<Void>> requestMonos = new ArrayList<>();
                            for (int pageNo = 1; pageNo <= totalPages; pageNo++) {
                                String url = buildUrl(endpoint, serviceKey, inqryBgnDt, inqryEndDt, pageNo);
                                requestMonos.add(fetchAndSaveData(url));
                            }

                            // 모든 요청이 완료될 때까지 기다림
                            return Flux.merge(requestMonos)
                                    .then(Mono.just(ResponseEntity.ok("{\"message\":\"All data fetched and saved successfully.\"}")));
                        } else {
                            logger.error("API Error: {}", resultMsg);
                            String errorResponse = String.format(
                                    "{\"response\": {\"header\": {\"resultCode\": \"%s\", \"resultMsg\": \"%s\"}}}",
                                    resultCode, resultMsg);
                            return Mono.just(ResponseEntity.status(200).body(errorResponse));
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing JSON response: ", e);
                        return Mono.just(ResponseEntity.status(500).body("{\"error\":\"Error parsing JSON response.\"}"));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error while fetching data: ", e);
                    return Mono.just(ResponseEntity.status(500).body("{\"error\":\"Error occurred while fetching data.\"}"));
                });
    }

    // 데이터 요청 및 저장 메소드
    private Mono<Void> fetchAndSaveData(String url) {
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        JsonNode bodyNode = jsonNode.path("response").path("body").path("items");

                        List<ContractInfo> contractInfos = new ArrayList<>();
                        for (JsonNode itemNode : bodyNode) {
                            ContractInfo contractInfo = objectMapper.treeToValue(itemNode, ContractInfo.class);
                            contractInfos.add(contractInfo);
                        }

                        // 데이터베이스에 저장
                        contractInfoRepository.saveAll(contractInfos);
                        logger.info("Saved data for URL: {}", url);
                        return Mono.empty();
                    } catch (Exception e) {
                        logger.error("Error parsing JSON response for URL: {}", url, e);
                        return Mono.error(new RuntimeException("Error parsing JSON response"));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error while fetching data for URL: {}", url, e);
                    return Mono.empty(); // 에러 발생 시 빈 결과를 반환
                }).then();
    }

    // URL 생성 메소드
    private String buildUrl(String endpoint, String serviceKey, String inqryBgnDt, String inqryEndDt, int pageNo) {
        return "https://apis.data.go.kr" + endpoint +
                "?serviceKey=" + serviceKey +
                "&numOfRows=100" +
                "&pageNo=" + pageNo +
                "&inqryDiv=1" +
                "&inqryBgnDt=" + inqryBgnDt +
                "&inqryEndDt=" + inqryEndDt +
                "&type=json";
    }
}