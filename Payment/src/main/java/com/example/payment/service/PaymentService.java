package com.example.payment.service;

import com.example.payment.dto.OrderRequestToPay;
import com.example.payment.model.PayedOrder;
import com.example.payment.model.Status;
import com.example.payment.repository.PayedOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PayedOrderRepository payedOrderRepository;
    private final KafkaTemplate<String, OrderRequestToPay> kafkaTemplate;

    @Autowired
    public PaymentService(PayedOrderRepository payedOrderRepository, KafkaTemplate<String, OrderRequestToPay> kafkaTemplate) {
        this.payedOrderRepository = payedOrderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @KafkaListener(topics = "new_orders", groupId = "payment-service-group")
    public void pay(OrderRequestToPay orderDTO) {

        Logger logger = LoggerFactory.getLogger(PaymentService.class);

        if (orderDTO.getStatus() != Status.NEW) {
            logger.error("Order status is not NEW: {}", orderDTO.getStatus());
            throw new IllegalStateException("Order must be NEW for payment!");
        }

        PayedOrder payedOrder = new PayedOrder();
        payedOrder.setOrderId(orderDTO.getOrderId());
        payedOrder.setProductName(orderDTO.getProductName());
        payedOrder.setQuantity(orderDTO.getQuantity());
        payedOrder.setStatus(Status.PAID);


        try {
            payedOrderRepository.save(payedOrder);
        } catch (DataAccessException e) {
            logger.error("Failed to save payedOrder: {}", payedOrder, e);
            throw new RuntimeException("Failed to save payedOrder", e);
        }

        orderDTO.setStatus(Status.PAID);


        try {
            kafkaTemplate.send("payed_orders", orderDTO);
            kafkaTemplate.send("order_status_updates", orderDTO);
        } catch (Exception e) {
            logger.error("Failed to send orderDTO to Kafka topics: {}", orderDTO, e);
            throw new RuntimeException("Failed to send orderDTO to Kafka topics", e);
        }

    }

}
