package com.orderup.dto;

import java.util.List;

// ── Incoming: place a new order ──────────────────────────────────────────────
public class OrderRequest {

    private String tableNo;
    private String orderType;        // DINE_IN | TAKEAWAY
    private String paymentMethod;    // CASH | CARD | E_WALLET
    private List<OrderItemRequest> items;

    public static class OrderItemRequest {
        private Long menuItemId;
        private int quantity;
        private String notes;

        public Long getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public String getTableNo() { return tableNo; }
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
