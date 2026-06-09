package com.orderup.dto;

import java.math.BigDecimal;
import java.util.List;

public class PaymentRequest {
    private String paymentMethod;
    private List<PaymentSplitRequest> splits;

    public static class PaymentSplitRequest {
        private String payerName;
        private BigDecimal amount;

        public String getPayerName() {
            return payerName;
        }

        public void setPayerName(String payerName) {
            this.payerName = payerName;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<PaymentSplitRequest> getSplits() {
        return splits;
    }

    public void setSplits(List<PaymentSplitRequest> splits) {
        this.splits = splits;
    }
}
