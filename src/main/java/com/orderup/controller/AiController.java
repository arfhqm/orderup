package com.orderup.controller;

import com.orderup.dto.RecommendRequest;
import com.orderup.dto.RecommendationResponse;
import com.orderup.service.AiRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AiController {

    private final AiRecommendationService aiService;

    public AiController(AiRecommendationService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<RecommendationResponse> recommend(@RequestBody RecommendRequest req) {
        return ResponseEntity.ok(aiService.recommend(req.getCartItemNames()));
    }
}
