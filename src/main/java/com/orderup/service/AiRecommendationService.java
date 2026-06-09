package com.orderup.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AiRecommendationService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String recommend(List<String> cartItems) {
        try {
            String itemList = String.join(", ", cartItems);
            String prompt = String.format(
                    "A customer at a café has ordered: %s. " +
                            "Suggest ONE short add-on they might enjoy (e.g. a drink, snack, or dessert). " +
                            "Keep it to one sentence, conversational, and café-appropriate.",
                    itemList);

            Map<String, Object> body = Map.of(
                    "model", "gpt-3.5-turbo",
                    "max_tokens", 80,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a friendly café assistant suggesting add-ons."),
                            Map.of("role", "user", "content", prompt)));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            return json.at("/choices/0/message/content").asText("No suggestion available.");

        } catch (Exception e) {
            return "Try pairing your order with our house-blend coffee!"; // graceful fallback
        }
    }
}
