package com.test.telegramBot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GeminiApiClient {

    private final String apiKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiApiClient(@Value("${geminiApiKey}") String apiKey,
                           WebClient.Builder webClientBuilder,
                           ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
        this.objectMapper = objectMapper;
    }

    public String getResponse(String prompt) {
        int retries = 3;
        int delay = 2000;

        for (int i = 0; i < retries; i++) {
            try {
                String json = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt + "\" }] }] }";

                Mono<String> responseMono = webClient.post()
                        .uri("/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(json)
                        .retrieve()
                        .bodyToMono(String.class);

                String responseBody = responseMono.block();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                return jsonNode.at("/candidates/0/content/parts/0/text").asText();
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("429")) {
                    System.out.println("Rate limit exceeded. Waiting " + delay + " ms...");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return "Request error: execution interrupted.";
                    }
                    delay = Math.min(delay * 2, 10000);
                }
            }
        }
        return "Request processing error. Please try again later.";
    }
}