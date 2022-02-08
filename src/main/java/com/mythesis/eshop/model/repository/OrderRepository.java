package com.mythesis.eshop.model.repository;

import com.mythesis.eshop.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    public List<Order> findAllByUserId(Long userId);
}
