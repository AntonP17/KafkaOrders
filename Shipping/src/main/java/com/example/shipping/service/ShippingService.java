package com.example.shipping.service;

import com.example.shipping.dto.OrderEvent;
import com.example.shipping.model.ShippingOrder;
import com.example.shipping.model.Status;
import com.example.shipping.repository.ShippingOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingService {

    private final ShippingOrderRepository shippingOrderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    public ShippingService(ShippingOrderRepository shippingOrderRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.shippingOrderRepository = shippingOrderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = "payed_orders", groupId = "shipping")
    public void shipOrder(OrderEvent event) {

        Logger logger = LoggerFactory.getLogger(ShippingService.class);

        if (event.getStatus() != Status.PAID) {
            logger.error("Order status is not PAID: {}", event.getStatus());
            throw new IllegalStateException("Order must be PAID for shipping!");
        }

        ShippingOrder shippingOrder = new ShippingOrder();
        shippingOrder.setOrderId(event.getOrderId());
        shippingOrder.setProductName(event.getProductName());
        shippingOrder.setQuantity(event.getQuantity());
        shippingOrder.setStatus(Status.PAID);

        try {
            shippingOrderRepository.save(shippingOrder);
        } catch (DataAccessException e) {
            logger.error("Failed to save shippingOrder: {}", shippingOrder, e);
            throw new RuntimeException("Failed to save shippingOrder", e);
        }

        event.setStatus(Status.SHIPPED);

        try {
            kafkaTemplate.send("sent_orders", event);
            kafkaTemplate.send("order_status_updates", event);
        } catch (Exception e) {
            logger.error("Failed to send event to Kafka topics: {}", event, e);
            throw new RuntimeException("Failed to send event to Kafka topics", e);
        }
    }


}
