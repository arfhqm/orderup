package com.orderup.dto;

import java.util.List;

public class RecommendRequest {
    private List<String> cartItemNames;   // just the names, e.g. ["Latte", "Croissant"]

    public List<String> getCartItemNames() { return cartItemNames; }
    public void setCartItemNames(List<String> cartItemNames) { this.cartItemNames = cartItemNames; }
}
