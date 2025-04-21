package com.example.notifications.service;

import com.example.notifications.dto.OrderEvent;
import com.example.notifications.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    public NotificationService(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "sent_orders", groupId = "notifications")
    public void sendNotification(OrderEvent event) {

        event.setStatus(Status.DELIVERED);
        kafkaTemplate.send("order_status_updates", event);

    }

}
