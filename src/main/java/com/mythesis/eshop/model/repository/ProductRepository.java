package com.mythesis.eshop.model.repository;

import com.mythesis.eshop.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT p " +
            "FROM User u, Order o, OrderItem oi, Product p " +
            "WHERE o.user.id = u.id AND " +
            "oi.order.id = o.id AND " +
            "oi.product.id = p.id AND " +
            "u.id = ?1")
    List<Product> findAllOrderedByUserId(Long userId);

    @Query("SELECT p " +
            "FROM User u, Cart c, CartItem ci, Product p " +
            "WHERE c.user.id = u.id AND " +
            "ci.cart.id = c.id AND " +
            "ci.product.id = p.id AND " +
            "u.id = ?1")
    List<Product> findAllInCartByUserId(Long userId);

    @Query("SELECT p " +
            "FROM User u, Cart c, CartItem ci, Order o, OrderItem oi, Product p " +
            "WHERE c.user.id = u.id AND " +
            "ci.cart.id = c.id AND " +
            "o.user.id = u.id AND " +
            "oi.order.id = o.id AND " +
            "oi.product.id = ci.product.id AND " +
            "ci.product.id = p.id AND " +
            "u.id = ?1")
    List<Product> findProductsOrderedAndInCartByUser(Long userId);
}
