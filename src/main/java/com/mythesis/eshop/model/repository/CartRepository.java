package com.mythesis.eshop.model.repository;

import com.mythesis.eshop.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    public List<Cart> findAllByUserId (Long userId);
}
