package com.example.shipping.service;

import com.example.shipping.dto.OrderEvent;
import com.example.shipping.model.ShippingOrder;
import com.example.shipping.model.Status;
import com.example.shipping.repository.ShippingOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    @KafkaListener(topics = "payed_orders")
    public void shipOrder(OrderEvent event) {

        // 1. Проверяем статус
        if (event.getStatus() != Status.PAID) {
            throw new IllegalStateException("Order must be PAID for shipping!");
        }

        // 2. Сохраняем в свою БД (опционально)
        ShippingOrder shippingOrder = new ShippingOrder();
        shippingOrder.setOrderId(event.getOrderId());
        shippingOrder.setProductName(event.getProductName());
        shippingOrder.setQuantity(event.getQuantity());
        shippingOrder.setStatus(Status.PAID);
        shippingOrderRepository.save(shippingOrder);

        // 3. Обновляем и отправляем событие
        event.setStatus(Status.SHIPPED);
        kafkaTemplate.send("sent_orders", event);
        kafkaTemplate.send("order_status_updates", event);
    }


}
