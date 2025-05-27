package org.example.g2bplatform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.g2bplatform.DTO.ContractInfoCnstwkDTO;
import org.example.g2bplatform.DTO.ContractInfoDTO;
import org.example.g2bplatform.DTO.ContractInfoDetailDTO;
import org.example.g2bplatform.DTO.ContractInfoServcDTO;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
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
        this.webClient = webClientBuilder
                .baseUrl("https://apis.data.go.kr")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
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
                .bodyToMono(String.class)
                .doOnNext(response -> logger.info("Received response: {}", response)) // 응답 로깅
                .flatMap(initialResponse -> {
                    logger.info("💬 Raw API response:\n{}", initialResponse); // 👈 요거 추가

                    if (initialResponse.startsWith("<")) {
                        logger.error("❌ HTML 응답 수신됨! (보통은 인증 문제, 잘못된 요청)");
                        return Mono.just(ResponseEntity.status(500).body("{\"error\":\"Received HTML instead of JSON.\"}"));
                    }

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
                            // 모든 페이지에 대한 요청을 병렬로 처리
                            return fetchAndSaveAllPages(endpoint, serviceKey, inqryBgnDt, inqryEndDt, totalPages)
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

    // 모든 페이지를 병렬로 처리하는 메소드
    private Mono<Void> fetchAndSaveAllPages(String endpoint, String serviceKey, String inqryBgnDt, String inqryEndDt, int totalPages) {
        return Flux.range(1, totalPages) // 1부터 totalPages까지의 페이지 번호 생성
                .flatMap(pageNo -> {
                    String url = buildUrl(endpoint, serviceKey, inqryBgnDt, inqryEndDt, pageNo);
                    return fetchAndSaveData(url, endpoint)
                            .doOnError(e -> logger.error("Error while processing page {}: {}", pageNo, e.getMessage())) // 에러 로깅
                            .retry(15); // 페이지 처리 오류 시 최대 설정횟수만큼 재시도
                }, 30) // 병렬로 처리할 최대 개수 설정
                .then(); // 모든 작업이 완료될 때까지 기다림
    }

    // 데이터 요청 및 저장 메소드
    private Mono<Void> fetchAndSaveData(String url, String endpoint) {
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(response);
                        JsonNode headerNode = jsonNode.path("response").path("header");
                        String resultCode = headerNode.path("resultCode").asText();
                        String resultMsg = headerNode.path("resultMsg").asText();

                        // API 요청이 성공했는지 확인
                        if (!"00".equals(resultCode)) {
                            logger.error("API Error for URL {}: {}", url, resultMsg);
                            return Mono.error(new RuntimeException("API Error: " + resultMsg));
                        }

                        JsonNode bodyNode = jsonNode.path("response").path("body").path("items");

                        // 엔드포인트에 따라 다른 처리 메소드 호출
                        if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThng".equals(endpoint)) { // 계약현황에 대한 물품조회
                            return processContractInfo(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngDetail".equals(endpoint)) { // 계약현황에 대한 물품세부조회
                            return processContractInfoDetail(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngChgHstry".equals(endpoint)) { // 계약현황에 대한 물품변경이력조회
                            return processContractInfoChangeHistory(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListThngPPSSrch".equals(endpoint)) { // 나라장터검색조건에 의한 계약현황 물품조회
                            return processContractInfoPPSSrch(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListCnstwk".equals(endpoint)) { // 계약현황에 대한 공사조회
                            return processContractInfoCnstwk(bodyNode);
                        } else if ("/1230000/ao/CntrctInfoService/getCntrctInfoListServc".equals(endpoint)) { // 계약현황에 대한 공사조회
                            return processContractInfoServc(bodyNode);
                        }else {
                            return Mono.empty(); // 해당 엔드포인트가 없을 경우 빈 응답 처리
                        }

                    } catch (Exception e) {
                        logger.error("Error parsing JSON response for URL: {}", url, e);
                        return Mono.error(new RuntimeException("Error parsing JSON response"));
                    }
                })
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof RuntimeException))
                .onErrorResume(e -> {
                    logger.error("Error while fetching data for URL: {}", url, e);
                    return Mono.empty();
                });
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

    // URL 생성 메소드
    private String buildUrl(String endpoint, String serviceKey, String inqryBgnDt, String inqryEndDt, int pageNo) {
        String beginDateParam = "inqryBgnDt"; // 기본적으로 inqryBgnDt 사용
        String endDateParam = "inqryEndDt";   // 기본적으로 inqryEndDt 사용

        // "나라장터검색조건에 의한 계약현황 물품조회" 엔드포인트일 경우 파라미터명을 inqryBgnDate와 inqryEndDate로 변경
        if ("/1230000/CntrctInfoService01/getCntrctInfoListThngPPSSrch01".equals(endpoint)) {
            beginDateParam = "inqryBgnDate";
            endDateParam = "inqryEndDate";
        }

        // URL 생성
        return "https://apis.data.go.kr" + endpoint +
                "?serviceKey=" + serviceKey +
                "&numOfRows=100" +
                "&pageNo=" + pageNo +
                "&inqryDiv=1" +
                "&" + beginDateParam + "=" + inqryBgnDt +
                "&" + endDateParam + "=" + inqryEndDt +
                "&type=json";
    }
}