package org.example.g2bplatform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController; // Add RestController annotation to make it a Spring REST controller

@Tag(name = "OAuth2", description = "Google OAuth2 인증 관련 API")
@RestController // Added @RestController
public class OAuth2Controller {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2Controller(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // TODO 스프레드시트 연동 후 매일 자동 호출 되도록 해야함
    @Operation(summary = "Google OAuth2 콜백", description = "Google OAuth2 인증 후 호출되는 콜백 엔드포인트. 액세스 토큰을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공 및 액세스 토큰 처리 완료"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/callback")
    public String callback(
            @Parameter(description = "OAuth2 인증 토큰", required = true)
            OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());

        String accessToken = client.getAccessToken().getTokenValue();

        // accessToken을 어딘가 저장해둬야 함 (ex. 세션, DB, 메모리)
        System.out.println("AccessToken: " + accessToken);

        return "구글 인증 성공! 이제 스프레드시트 API를 호출할 수 있습니다.";
    }
}
