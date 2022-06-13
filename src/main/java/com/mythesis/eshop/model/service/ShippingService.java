package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {

    public void ship(Order order) {
        Integer numberOfProducts = order.getOrderItems().size();

        if (numberOfProducts > 10) {
            throw new IllegalStateException("We cannot serve more than 10 products at this time");
        }

    }
}
