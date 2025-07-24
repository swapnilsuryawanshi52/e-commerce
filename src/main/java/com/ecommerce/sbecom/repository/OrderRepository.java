package com.ecommerce.sbecom.repository;

import com.ecommerce.sbecom.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
