package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.OrderItemEntryDTO;
import com.mythesis.eshop.dto.OrderItemInfoDTO;
import com.mythesis.eshop.model.entity.OrderItem;
import com.mythesis.eshop.model.service.OrderItemService;
import com.mythesis.eshop.util.OrderItemMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/orderItems")
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;

    public OrderItemController(OrderItemService orderItemService, OrderItemMapper orderItemMapper) {
        this.orderItemService = orderItemService;
        this.orderItemMapper = orderItemMapper;
    }

    @GetMapping(path = "/{orderItemId}")
    public OrderItemInfoDTO getOrderItem(@PathVariable("orderItemId") Long orderItemId){
        return orderItemMapper.toOrderItemInfoDto(
                orderItemService.retrieveById(orderItemId)
        );
    }

    @GetMapping
    public List<OrderItemInfoDTO> getOrderItems(){
        return orderItemService.retrieveAll()
                .stream()
                .map(orderItemMapper::toOrderItemInfoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public OrderItemInfoDTO createOrderItem(@RequestBody OrderItemEntryDTO orderItem){
            OrderItem mapped = orderItemMapper.fromOrderItemEntryDto(orderItem);

            return orderItemMapper.toOrderItemInfoDto(orderItemService.add(mapped));
    }

    @PutMapping(path = "/{orderItemId}")
    public OrderItemInfoDTO updateOrderItem(@PathVariable("orderItemId") Long orderItemId,
                                            @RequestBody OrderItemEntryDTO orderItem){
        OrderItem mapped = orderItemMapper.fromOrderItemEntryDto(orderItem);

        return orderItemMapper.toOrderItemInfoDto(orderItemService.update(orderItemId,mapped));
    }

    @DeleteMapping(path = "/{orderItemId}")
    public void deleteOrderItem(@PathVariable("orderItemId") Long orderItemId){
        orderItemService.deleteById(orderItemId);
    }
}
