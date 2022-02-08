package com.mythesis.eshop.model.repository;

import com.mythesis.eshop.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

   public List<OrderItem> findAllByOrderId (Long orderId);
}
