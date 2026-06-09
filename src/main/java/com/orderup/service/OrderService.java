package com.orderup.service;

import com.orderup.dto.OrderRequest;
import com.orderup.dto.PaymentRequest;
import com.orderup.entity.MenuItem;
import com.orderup.entity.Order;
import com.orderup.entity.OrderItem;
import com.orderup.entity.Payment;
import com.orderup.repository.MenuItemRepository;
import com.orderup.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public Order placeOrder(OrderRequest req) {
        Order order = new Order();
        applyOrderHeader(order, req);
        replaceOrderItems(order, req.getItems());
        return orderRepository.save(order);
    }

    @Transactional
    public Order editOrder(Long id, OrderRequest req) {
        Order order = getOrderById(id);
        if (order.getStatus() == Order.Status.COMPLETED || order.getStatus() == Order.Status.CANCELLED) {
            throw new IllegalStateException("Completed or cancelled orders cannot be edited.");
        }
        if (order.getPaymentStatus() != Order.PaymentStatus.UNPAID) {
            throw new IllegalStateException("Paid orders cannot be edited.");
        }

        applyOrderHeader(order, req);
        order.getOrderItems().clear();
        replaceOrderItems(order, req.getItems());
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public Order updateStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(Order.Status.valueOf(status));
        return orderRepository.save(order);
    }

    @Transactional
    public Order payOrder(Long id, PaymentRequest req) {
        Order order = getOrderById(id);
        Order.PaymentMethod method = Order.PaymentMethod.valueOf(req.getPaymentMethod());
        BigDecimal paymentTotal = BigDecimal.ZERO;

        if (req.getSplits() == null || req.getSplits().isEmpty()) {
            throw new IllegalArgumentException("Payment needs at least one split.");
        }

        for (PaymentRequest.PaymentSplitRequest split : req.getSplits()) {
            if (split.getAmount() == null || split.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Each split amount must be positive.");
            }

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setPayerName(split.getPayerName());
            payment.setPaymentMethod(method);
            payment.setAmount(split.getAmount());
            order.getPayments().add(payment);
            paymentTotal = paymentTotal.add(split.getAmount());
        }

        BigDecimal newPaidAmount = order.getPaidAmount().add(paymentTotal);
        if (newPaidAmount.compareTo(order.getTotalAmount()) > 0) {
            throw new IllegalArgumentException("Payment exceeds order total.");
        }

        order.setPaymentMethod(method);
        order.setPaidAmount(newPaidAmount);
        if (newPaidAmount.compareTo(order.getTotalAmount()) < 0) {
            order.setPaymentStatus(Order.PaymentStatus.PARTIALLY_PAID);
        } else {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.Status.COMPLETED);
        }

        return orderRepository.save(order);
    }

    private void applyOrderHeader(Order order, OrderRequest req) {
        order.setTableNo(req.getTableNo());
        order.setOrderType(parseEnum(req.getOrderType(), Order.OrderType.DINE_IN));
        if (req.getPaymentMethod() != null && !req.getPaymentMethod().isBlank()) {
            order.setPaymentMethod(Order.PaymentMethod.valueOf(req.getPaymentMethod()));
        }
    }

    private void replaceOrderItems(Order order, List<OrderRequest.OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new IllegalArgumentException("Order needs at least one item.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderRequest.OrderItemRequest itemReq : itemRequests) {
            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero.");
            }

            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemReq.getMenuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setNotes(itemReq.getNotes());

            BigDecimal subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderItem.setSubtotal(subtotal);

            order.getOrderItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
    }

    private <T extends Enum<T>> T parseEnum(String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Enum.valueOf(fallback.getDeclaringClass(), value);
    }
}
