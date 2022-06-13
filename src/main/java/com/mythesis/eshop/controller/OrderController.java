package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.service.OrderService;
import com.mythesis.eshop.util.OrderMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping(path = "/{orderId}")
    public OrderInfoDTO getOrder(@PathVariable("orderId") Long orderId) {
        return orderMapper.toOrderInfoDto(orderService.retrieveById(orderId));
    }

    @GetMapping
    public List<OrderInfoDTO> getOrders() {
        return orderService.retrieveAll()
                .stream()
                .map(orderMapper::toOrderInfoDto)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/stats")
    public void getStats(){
       orderService.stats();
    }

    @PostMapping
    public OrderInfoDTO createOrder(@RequestBody OrderEntryDTO order){
        Order mappedOrder = orderMapper.fromOrderEntryDto(order);
        Order createdOrder = orderService.createOrder(mappedOrder);

        return orderMapper.toOrderInfoDto(createdOrder);
    }

    @PutMapping(path = "/{orderId}")
    public OrderInfoDTO updateOrder(@PathVariable("orderId") Long orderId,
                             @RequestBody OrderEntryDTO order){

        Order mappedOrder = orderMapper.fromOrderEntryDto(order);
        Order updatedOrder = orderService.updateOrder(orderId,mappedOrder);

        return  orderMapper.toOrderInfoDto(updatedOrder);

    }

    @DeleteMapping(path = "/{orderId}")
    public void deleteOrder(@PathVariable("orderId") Long orderId){

        orderService.deleteOrder(orderId);
    }
}
