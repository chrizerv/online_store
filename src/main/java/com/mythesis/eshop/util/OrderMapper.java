package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    private ModelMapper modelMapper;
    private UserService userService;

    @Autowired
    public OrderMapper(ModelMapper modelMapper, UserService userService) {
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    public OrderInfoDTO toOrderInfoDto(Order order){
        OrderInfoDTO mapped = modelMapper.map(order, OrderInfoDTO.class);
        mapped.setUserId(order.getUser().getId());
        return mapped;
    }


    public Order fromOrderEntryDto(OrderEntryDTO order){
        Order mappedOrder = modelMapper.map(order, Order.class);

        if (order.getUserId() != null){
            User user = userService.retrieveById(order.getUserId());
            mappedOrder.setUser(user);

        }
        return mappedOrder;
    }

    public void copyProperties(Order source, Order destination){
        modelMapper.map(source,destination);

    }
}
