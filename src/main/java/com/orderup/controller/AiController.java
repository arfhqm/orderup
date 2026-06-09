package com.orderup.controller;

import com.orderup.dto.RecommendRequest;
import com.orderup.service.AiRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AiController {

    private final AiRecommendationService aiService;

    public AiController(AiRecommendationService aiService) {
        this.aiService = aiService;
    }

    // POST /api/ai/recommend
    // Body: { "cartItemNames": ["Latte", "Croissant"] }
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommend(@RequestBody RecommendRequest req) {
        String suggestion = aiService.recommend(req.getCartItemNames());
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
}
