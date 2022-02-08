package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.OrderItemEntryDTO;
import com.mythesis.eshop.dto.OrderItemInfoDTO;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.OrderItem;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.OrderService;
import com.mythesis.eshop.model.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    private ModelMapper modelMapper;
    private ProductService productService;
    private OrderService orderService;

    @Autowired
    public OrderItemMapper(ModelMapper modelMapper,
                           ProductService productService,
                           OrderService orderService) {
        this.modelMapper = modelMapper;
        this.productService = productService;
        this.orderService = orderService;
    }

    public OrderItemInfoDTO toOrderItemInfoDto(OrderItem orderItem){
        OrderItemInfoDTO mapped = modelMapper.map(orderItem, OrderItemInfoDTO.class);
        mapped.setOrderId(orderItem.getOrder().getId());
        return mapped;
    }


    public OrderItem fromOrderItemEntryDto(OrderItemEntryDTO orderItem){
        OrderItem mapped = modelMapper.map(orderItem, OrderItem.class);

        if (orderItem.getOrderId() != null){
            Order order = orderService.retrieveById(orderItem.getOrderId());
            mapped.setOrder(order);
        }

        if (orderItem.getProductId() != null){
            Product product = productService.retrieveById(orderItem.getProductId());
            mapped.setProduct(product);
        }
        System.out.println(mapped);
        return mapped;
    }

    public void copyProperties(OrderItem source, OrderItem destination){
        modelMapper.map(source,destination);

    }


}
