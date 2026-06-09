package com.orderup.service;

import com.orderup.dto.OrderRequest;
import com.orderup.entity.MenuItem;
import com.orderup.entity.Order;
import com.orderup.entity.OrderItem;
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
        order.setTableNo(req.getTableNo());
        order.setOrderType(Order.OrderType.valueOf(req.getOrderType()));
        order.setPaymentMethod(Order.PaymentMethod.valueOf(req.getPaymentMethod()));

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemReq.getMenuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setNotes(itemReq.getNotes());

            BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderItem.setSubtotal(subtotal);

            order.getOrderItems().add(orderItem);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
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
}
