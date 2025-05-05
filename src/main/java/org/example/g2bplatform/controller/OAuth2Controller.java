package org.example.g2bplatform.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;

public class OAuth2Controller {
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2Controller(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    // TODO 스프레드시트 연동 후 매일 자동 호출 되도록 해야함
    @GetMapping("/callback")
    public String callback(OAuth2AuthenticationToken authentication) {
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
