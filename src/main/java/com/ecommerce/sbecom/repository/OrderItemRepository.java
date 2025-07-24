package com.ecommerce.sbecom.repository;

import com.ecommerce.sbecom.entity.Order;
import com.ecommerce.sbecom.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
