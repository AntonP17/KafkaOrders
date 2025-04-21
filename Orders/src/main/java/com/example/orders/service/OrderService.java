package com.example.orders.service;

import com.example.orders.dto.OrderEvent;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.model.Status;
import com.example.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {

        Order order = new Order();
        order.setProductName(orderRequest.getProductName());
        order.setQuantity(orderRequest.getQuantity());
        order.setStatus(Status.NEW);

        order = orderRepository.save(order); // Получаем order с ID

        // Собираем DTO для Kafka
        OrderEvent event = new OrderEvent();
        event.setOrderId(order.getId());
        event.setProductName(order.getProductName());
        event.setQuantity(order.getQuantity());
        event.setStatus(order.getStatus());

        kafkaTemplate.send("new_orders", event); // Отправляем event, а не orderRequest

        return order;
    }

    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId).orElse(null);

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setProductName(order.getProductName());
        orderResponse.setQuantity(order.getQuantity());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setOrderId(order.getId());

        return orderResponse;
    }

    @Transactional
    @KafkaListener(topics = "order_status_updates",  groupId = "order-service-group-id")
    public void updateOrderStatus(OrderEvent orderEvent) {

        Order order = orderRepository.findById(orderEvent.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderEvent.getOrderId()));

        order.setStatus(orderEvent.getStatus());

    }



}
