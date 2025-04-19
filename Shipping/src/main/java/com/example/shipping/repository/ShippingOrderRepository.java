package com.example.shipping.repository;

import com.example.shipping.model.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrder, Long> {
}
