package com.example.notifications.service;

import com.example.notifications.dto.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @KafkaListener(topics = "sent_orders")
    public void sendNotification(OrderEvent event) {




    }

}
