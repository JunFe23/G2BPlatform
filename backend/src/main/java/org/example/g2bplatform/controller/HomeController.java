package org.example.g2bplatform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.g2bplatform.DTO.*;
import org.example.g2bplatform.exception.ApiHttpException;
import org.example.g2bplatform.mapper.DataDownloadMapper;
import org.example.g2bplatform.mapper.DataMapper;
import org.example.g2bplatform.model.*;
import org.example.g2bplatform.repository.*;
import org.example.g2bplatform.service.DataDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private final WebClient webClient;
    private final ContractInfoRepository contractInfoRepository;
    private final ContractInfoDetailRepository contractInfoDetailRepository;
    private final ContractInfoChangeHistoryRepository contractInfoChangeHistoryRepository;
    private final ContractInfoPPSSrchRepository contractInfoPPSSrchRepository;
    private final ContractInfoCnstwkRepository contractInfoCnstwkRepository;
    private final ContractInfoServcRepository contractInfoServcRepository;
    private final ObjectMapper objectMapper;

    private final DataDownloadService dataDownloadService;

    // 생성자에서 ObjectMapper를 주입받도록 수정
    public HomeController(WebClient.Builder webClientBuilder,
                          ContractInfoRepository contractInfoRepository,
                          ContractInfoDetailRepository contractInfoDetailRepository,
                          ContractInfoChangeHistoryRepository contractInfoChangeHistoryRepository,
                          ContractInfoPPSSrchRepository contractInfoPPSSrchRepository,
                          ContractInfoCnstwkRepository contractInfoCnstwkRepository,
                          ContractInfoServcRepository contractInfoServcRepository,
                          ObjectMapper objectMapper,
                          DataDownloadService dataDownloadService) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB로 설정
                .build();

        this.webClient = webClientBuilder
                .baseUrl("https://apis.data.go.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();

        this.contractInfoRepository = contractInfoRepository;
        this.contractInfoDetailRepository =  contractInfoDetailRepository;
        this.contractInfoChangeHistoryRepository =  contractInfoChangeHistoryRepository;
        this.contractInfoPPSSrchRepository = contractInfoPPSSrchRepository;
        this.contractInfoCnstwkRepository = contractInfoCnstwkRepository;
        this.contractInfoServcRepository = contractInfoServcRepository;
        this.objectMapper = objectMapper; // 주입받은 ObjectMapper 사용
        this.dataDownloadService = dataDownloadService;
    }

    @PostMapping("/fetch")
    @Transactional
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
                .onStatus(s -> s.isError(), cr ->
                        cr.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> {
                                    logger.error("⬇️ Upstream HTTP {} for initialUrl \n{}", cr.statusCode().value(), body);
                                    return Mono.error(new ApiHttpException(cr.statusCode().value(), body, "Upstream HTTP error"));
                                })
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(45))
                .onErrorMap(java.util.concurrent.TimeoutException.class, e ->
                        new ApiHttpException(
                                598,                               // 임의의 네트워크 타임아웃 코드
                                "request timed out (>=30s)",       // upstreamBody
                                "Network timeout while calling upstream API"
                        )
                )
                .doOnNext(resp -> logger.info("Received response: {}", resp))
                .flatMap(initialResponse -> {
                    logger.info("💬 Raw API response:\n{}", initialResponse);

                    if (initialResponse.startsWith("<")) {
                        logger.error("❌ HTML 응답 수신됨! url={}", initialUrl);
                        return Mono.error(new ApiHttpException(502, initialResponse, "Received HTML instead of JSON"));
                    }

                    try {
                        JsonNode initialJsonNode = objectMapper.readTree(initialResponse);
                        JsonNode headerNode = initialJsonNode.path("response").path("header");
                        String resultCode = headerNode.path("resultCode").asText(null);
                        String resultMsg  = headerNode.path("resultMsg").asText(null);
                        int totalCount = initialJsonNode.path("response").path("body").path("totalCount").asInt(0);

                        if (!"00".equals(resultCode)) {
                            logger.error("API Error: {} - {}", resultCode, resultMsg);
                            // 프런트가 그대로 볼 수 있도록 200 바디로 반환
                            String errorResponse = String.format(
                                    "{\"response\": {\"header\": {\"resultCode\": \"%s\", \"resultMsg\": \"%s\"}}}",
                                    String.valueOf(resultCode), String.valueOf(resultMsg)
                            );
                            return Mono.just(ResponseEntity.status(200).body(errorResponse));
                        }

                        int numOfRows = 100;
                        int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
                        logger.info("Total Count: {}, Total Pages: {}", totalCount, totalPages);

                        return fetchAndSaveAllPages(endpoint, serviceKey, inqryBgnDt, inqryEndDt, totalPages)
                                .then(Mono.just(ResponseEntity.ok("{\"message\":\"All data fetched and saved successfully.\"}")));

                    } catch (Exception e) {
                        logger.error("Error parsing JSON response: raw={}", initialResponse, e);
                        return Mono.error(new ApiHttpException(200, initialResponse, "Error parsing JSON response"));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Error while fetching data (initial): ", e);
                    if (e instanceof ApiHttpException ahe) {
                        Map<String, Object> payload = new LinkedHashMap<>();
                        payload.put("error", ahe.getMessage());
                        payload.put("status", ahe.getStatus());
                        payload.put("upstreamBody", ahe.getBody());
                        return Mono.just(ResponseEntity.status(502).body(new ObjectMapper().valueToTree(payload).toString()));
                    }
                    return Mono.just(ResponseEntity.status(500).body("{\"error\":\"Error occurred while fetching data.\"}"));
                });
    }

    // 모든 페이지를 병렬로 처리하는 메소드
    private Mono<Void> fetchAndSaveAllPages(String endpoint, String serviceKey, String inqryBgnDt, String inqryEndDt, int totalPages) {
        return Flux.range(1, totalPages) // 1부터 totalPages까지의 페이지 번호 생성
                .delayElements(Duration.ofMillis(120))
                .flatMap(pageNo -> {
                    String url = buildUrl(endpoint, serviceKey, inqryBgnDt, inqryEndDt, pageNo);
                    logger.info("➡️ fetching page {}/{}: {}", pageNo, totalPages, url);
                    return fetchAndSaveData(url, endpoint)
                            .doOnError(e -> logger.error("페이지 {} 처리 실패: {}", pageNo, e.getMessage()))
                            .retry(15); // 페이지 처리 오류 시 최대 설정횟수만큼 재시도
                }, 6) // 병렬로 처리할 최대 개수 설정
                .then(); // 모든 작업이 완료될 때까지 기다림
    }

    // 데이터 요청 및 저장 메소드
    private Mono<Void> fetchAndSaveData(String url, String endpoint) {
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .onStatus(s -> s.isError(), cr ->
                        cr.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> {
                                    logger.error("⬇️ Upstream HTTP {} for {} \n{}", cr.statusCode().value(), url, body);
                                    return Mono.error(new ApiHttpException(cr.statusCode().value(), body, "Upstream HTTP error"));
                                })
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(45))
                .onErrorMap(java.util.concurrent.TimeoutException.class, e ->
                        new ApiHttpException(
                                598,                               // 임의의 네트워크 타임아웃 코드
                                "request timed out (>=30s)",       // upstreamBody
                                "Network timeout while calling upstream API"
                        )
                )
                .flatMap(response -> {
                    if (response.startsWith("<")) {
                        logger.error("❌ HTML 응답 수신. url={}", url);
                        return Mono.error(new ApiHttpException(502, response, "Received HTML instead of JSON"));
                    }
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        JsonNode headerNode = jsonNode.path("response").path("header");
                        String resultCode = headerNode.path("resultCode").asText(null);
                        String resultMsg  = headerNode.path("resultMsg").asText(null);

                        if (!"00".equals(resultCode)) {
                            logger.error("API Logic Error {} - {} for {}", resultCode, resultMsg, url);
                            return Mono.error(new ApiHttpException(200, response,
                                    "API returned non-success resultCode: " + resultCode));
                        }

                        JsonNode bodyNode = jsonNode.path("response").path("body").path("items");
                        if (!bodyNode.isArray()) {
                            logger.error("❌ items 배열 없음. raw={}", response);
                            return Mono.error(new ApiHttpException(200, response, "Missing items array"));
                        }

                        // 엔드포인트 라우팅
                        if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThng".equals(endpoint)) {
                            return processContractInfo(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngDetail".equals(endpoint)) {
                            return processContractInfoDetail(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngChgHstry".equals(endpoint)) {
                            return processContractInfoChangeHistory(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngPPSSrch".equals(endpoint)) {
                            return processContractInfoPPSSrch(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListCnstwk".equals(endpoint)) {
                            return processContractInfoCnstwk(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListServc".equals(endpoint)) {
                            return processContractInfoServc(bodyNode);
                        } else if ("/1230000/at/ShoppingMallPrdctInfoService/getShoppingMallPrdctInfoList".equals(endpoint)) {
                            return processContractShoppingmall(bodyNode);
                        } else {
                            logger.warn("알 수 없는 endpoint: {}", endpoint);
                            return Mono.empty();
                        }

                    } catch (Exception e) {
                        logger.error("JSON 파싱 실패 url={} raw={}", url, response, e);
                        return Mono.error(new ApiHttpException(200, response, "Error parsing JSON"));
                    }
                })
                .retryWhen(
                        reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(2))
                                .maxBackoff(Duration.ofSeconds(15))
                                .jitter(0.3)
                                .filter(ex ->
                                        (ex instanceof ApiHttpException ahe && (ahe.getStatus() == 598 || ahe.getStatus() == 599)) // timeout/handshake
                                                || (ex instanceof ApiHttpException ahe2 && (ahe2.getStatus() == 429 || (ahe2.getStatus() >= 500 && ahe2.getStatus() < 600)))
                                )
                )
                .doOnError(e -> logger.error("❌ fetchAndSaveData 실패 url={}", url, e));
    }

    // ContractInfo 엔티티 처리
    private Mono<Void> processContractInfo(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoDTO> contractInfos = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoDTO contractInfo = objectMapper.treeToValue(itemNode, ContractInfoDTO.class);
            contractInfos.add(contractInfo);
        }
        return dataDownloadService.insertContractInfo(contractInfos);
    }

    // ContractInfoDetail 엔티티 처리
    private Mono<Void> processContractInfoDetail(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoDetailDTO> contractInfoDetails = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoDetailDTO contractInfoDetail = objectMapper.treeToValue(itemNode, ContractInfoDetailDTO.class);
            contractInfoDetails.add(contractInfoDetail);
        }
        return dataDownloadService.insertContractInfoDetails(contractInfoDetails);
    }

    // ContractInfoChangeHistory 엔티티 처리
    private Mono<Void> processContractInfoChangeHistory(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoChangeHistory> contractInfoChangeHistories = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoChangeHistory contractInfoChangeHistory = objectMapper.treeToValue(itemNode, ContractInfoChangeHistory.class);
            contractInfoChangeHistories.add(contractInfoChangeHistory);
        }
        return Flux.fromIterable(contractInfoChangeHistories)
                .buffer(2000)
                .flatMap(batch -> Mono.fromRunnable(() -> contractInfoChangeHistoryRepository.saveAll(batch)))
                .then();
    }

    // ContractInfoPPSSrch 처리
    private Mono<Void> processContractInfoPPSSrch(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoPPSSrch> contractInfoPPSSrches = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoPPSSrch contractInfoPPSSrch = objectMapper.treeToValue(itemNode, ContractInfoPPSSrch.class); // ContractInfoPPSSrch 엔티티로 변환
            contractInfoPPSSrches.add(contractInfoPPSSrch);
        }
        return Flux.fromIterable(contractInfoPPSSrches)
                .buffer(2000) // 대량 데이터 처리 시 배치 처리
                .flatMap(batch -> Mono.fromRunnable(() -> contractInfoPPSSrchRepository.saveAll(batch))) // 저장 처리
                .then();
    }

    // ContractInfoCnstwk 처리
    private Mono<Void> processContractInfoCnstwk(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoCnstwkDTO> contractInfoCnstwks = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoCnstwkDTO contractInfoCnstwk = objectMapper.treeToValue(itemNode, ContractInfoCnstwkDTO.class); // ContractInfoCnstwk 엔티티로 변환
            contractInfoCnstwks.add(contractInfoCnstwk);
        }
        return dataDownloadService.ContractInfoCnstwk(contractInfoCnstwks);
    }

    // ContractInfoServc 처리
    private Mono<Void> processContractInfoServc(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractInfoServcDTO> contractInfoServcs = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractInfoServcDTO contractInfoServc = objectMapper.treeToValue(itemNode, ContractInfoServcDTO.class); // ContractInfoCnstwk 엔티티로 변환
            contractInfoServcs.add(contractInfoServc);
        }
        return dataDownloadService.ContractInfoServc(contractInfoServcs);
    }

    // ContractInfoShppingmall 처리
    private Mono<Void> processContractShoppingmall(JsonNode bodyNode) throws JsonProcessingException {
        List<ContractShoppingmallDTO> contractInfoShoppingmalls = new ArrayList<>();
        for (JsonNode itemNode : bodyNode) {
            ContractShoppingmallDTO contractInfoShoppingmall = objectMapper.treeToValue(itemNode, ContractShoppingmallDTO.class); // ContractInfoCnstwk 엔티티로 변환
            contractInfoShoppingmalls.add(contractInfoShoppingmall);
        }
        return dataDownloadService.ContractInfoShoppingmall(contractInfoShoppingmalls);
    }

    // URL 생성 메소드
    private String buildUrl(String endpoint, String serviceKey, String inqryBgnDt, String inqryEndDt, int pageNo) {
        String beginDateParam = "inqryBgnDt";
        String endDateParam   = "inqryEndDt";
        if ("/1230000/CntrctInfoService01/getCntrctInfoListThngPPSSrch01".equals(endpoint)) {
            beginDateParam = "inqryBgnDate";
            endDateParam   = "inqryEndDate";
        }

        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl("https://apis.data.go.kr" + endpoint)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", pageNo)
                .queryParam("inqryDiv", 1)
                .queryParam("type", "json");

        if (inqryBgnDt != null && !inqryBgnDt.isBlank()) b.queryParam(beginDateParam, inqryBgnDt);
        if (inqryEndDt != null && !inqryEndDt.isBlank()) b.queryParam(endDateParam, inqryEndDt);

        // 이미 인코딩된 값 허용
        return b.build(true).toUriString();
    }
}