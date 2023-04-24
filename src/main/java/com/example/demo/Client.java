package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Slf4j
public class Client {
    private static RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        WebClient client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Flux<String> response = client.get()
                .uri("http://localhost:8080/rbe")
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToFlux(resp -> resp.bodyToFlux(String.class));
        response.subscribe(str -> log.info("Str = {}", str));
        var resp = response.blockFirst();
        log.info("Resp = {}", resp);
    }
}