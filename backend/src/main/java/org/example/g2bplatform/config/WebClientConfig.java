package org.example.g2bplatform.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.HTTP11) // ← HTTP/1.1로 강제
                .secure(ssl -> ssl.sslContext(
                        SslContextBuilder.forClient()
                                .protocols("TLSv1.2") // ← TLS 1.2 고정 (상대 서버 호환성 ↑)
                ))
                .responseTimeout(Duration.ofSeconds(30))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS));
                });

        return builder
                .defaultHeader(HttpHeaders.USER_AGENT, "G2BPlatform/1.0 (+contact:you@example.com)")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}