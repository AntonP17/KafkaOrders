package com.example.payment.repository;

import com.example.payment.model.PayedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayedOrderRepository extends JpaRepository<PayedOrder, Long> {
}
