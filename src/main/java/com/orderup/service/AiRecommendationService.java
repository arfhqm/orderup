package com.orderup.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderup.dto.RecommendationResponse;
import com.orderup.entity.MenuItem;
import com.orderup.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiRecommendationService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.api.model:gpt-4o-mini}")
    private String model;

    private final MenuItemRepository menuItemRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiRecommendationService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public RecommendationResponse recommend(List<String> cartItems) {
        MenuItem item = chooseAddon(cartItems == null ? List.of() : cartItems);
        String fallback = "Add " + item.getName() + " - it pairs well with this order.";

        if (!hasApiKey()) {
            return new RecommendationResponse(fallback, item.getName(), "local-rules");
        }

        try {
            String prompt = "Cart items: " + String.join(", ", cartItems) + ". Recommended add-on: "
                    + item.getName() + ". Write one short café POS upsell sentence. No extra options.";

            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", 60,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You help café staff suggest practical add-ons."),
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            String suggestion = json.at("/choices/0/message/content").asText(fallback).trim();
            return new RecommendationResponse(suggestion, item.getName(), "openai");
        } catch (Exception e) {
            return new RecommendationResponse(fallback, item.getName(), "local-rules");
        }
    }

    private MenuItem chooseAddon(List<String> cartItems) {
        List<MenuItem> menu = menuItemRepository.findByAvailableTrue();
        List<String> normalizedCart = cartItems.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .toList();

        boolean hasDrink = hasCategory(normalizedCart, menu, "Drinks");
        boolean hasFood = hasCategory(normalizedCart, menu, "Food");
        boolean hasDessert = hasCategory(normalizedCart, menu, "Desserts");

        String targetCategory = !hasDrink ? "Drinks" : !hasFood ? "Food" : !hasDessert ? "Desserts" : "Desserts";

        return menu.stream()
                .filter(item -> item.getCategory().equals(targetCategory))
                .filter(item -> !normalizedCart.contains(item.getName().toLowerCase(Locale.ROOT)))
                .min(Comparator.comparing(MenuItem::getPrice))
                .orElseGet(() -> menu.stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Menu has no available items.")));
    }

    private boolean hasCategory(List<String> normalizedCart, List<MenuItem> menu, String category) {
        return menu.stream()
                .filter(item -> item.getCategory().equals(category))
                .map(item -> item.getName().toLowerCase(Locale.ROOT))
                .anyMatch(normalizedCart::contains);
    }

    private boolean hasApiKey() {
        return apiKey != null
                && !apiKey.isBlank()
                && !apiKey.equals("your_openai_key_here");
    }
}
