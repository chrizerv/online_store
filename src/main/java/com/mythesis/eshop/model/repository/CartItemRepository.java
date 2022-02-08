package com.mythesis.eshop.model.repository;

import com.mythesis.eshop.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    public List<CartItem> findAllByCartId (Long cartId);
}
