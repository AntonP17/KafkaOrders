package com.example.orders.service;

import com.example.orders.dto.OrderEvent;
import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.model.Status;
import com.example.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {

//        Logger logger = LoggerFactory.getLogger(OrderService.class);


        Order order = new Order();
        order.setProductName(orderRequest.getProductName());
        order.setQuantity(orderRequest.getQuantity());
        order.setStatus(Status.NEW);

        try {
            order = orderRepository.save(order);
        } catch (DataAccessException e) {
            logger.error("Failed to save order: {}", orderRequest, e);
            throw new RuntimeException("Failed to save order", e);
        }

        OrderEvent event = new OrderEvent();
        event.setOrderId(order.getId());
        event.setProductName(order.getProductName());
        event.setQuantity(order.getQuantity());
        event.setStatus(order.getStatus());

        try {
            kafkaTemplate.send("new_orders", event);
        } catch (Exception e) {
            logger.error("Failed to send order event to Kafka: {}", event, e);
            throw new RuntimeException("Failed to send order event to Kafka", e);
        }

        return order;
    }

    public OrderResponse  getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            OrderResponse errorResponse = new OrderResponse();
            errorResponse.setErrorMessage("Заказ с ID " + orderId + " не найден");
            return errorResponse;
        }

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setProductName(order.getProductName());
        response.setQuantity(order.getQuantity());
        response.setStatus(order.getStatus());

        return response;

    }

    @Transactional
    @KafkaListener(topics = "order_status_updates",  groupId = "order-service-group-id")
    public void updateOrderStatus(OrderEvent orderEvent) {


        logger.info("Received order status update event: {}", orderEvent);

        try {
            Order order = orderRepository.findById(orderEvent.getOrderId())
                    .orElseThrow(() -> {
                        String errorMessage = "Order not found: " + orderEvent.getOrderId();
                        logger.error(errorMessage);
                        return new IllegalArgumentException(errorMessage);
                    });

            logger.info("Order found: {}", order);
            order.setStatus(orderEvent.getStatus());
            logger.info("Order status updated to: {}", orderEvent.getStatus());
            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Failed to update order status for order ID: {}", orderEvent.getOrderId(), e);
        }
    }
}
