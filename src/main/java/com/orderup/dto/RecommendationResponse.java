package com.orderup.dto;

public class RecommendationResponse {
    private String suggestion;
    private String recommendedItem;
    private String source;

    public RecommendationResponse(String suggestion, String recommendedItem, String source) {
        this.suggestion = suggestion;
        this.recommendedItem = recommendedItem;
        this.source = source;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getRecommendedItem() {
        return recommendedItem;
    }

    public void setRecommendedItem(String recommendedItem) {
        this.recommendedItem = recommendedItem;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
