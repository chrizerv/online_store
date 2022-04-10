package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.repository.OrderRepository;
import com.mythesis.eshop.util.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class OrderService {

    private OrderRepository orderRepository;
    private Validator validator;
    private OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        Validator validator,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.validator = validator;
        this.orderMapper = orderMapper;
    }

    public List<Order> retrieveAll (){
        return  orderRepository.findAll();
    }

    public List<Order> retrieveAllByUserId (Long userId){
        return orderRepository.findAllByUserId(userId);
    }

    public Order retrieveById (Long orderId){
        Order order;
        try {
            order = orderRepository.findById(orderId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Order");
        }
        return order;

    }

    public Order createOrder(Order order){

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return orderRepository.save(order);
    }

    public Order updateOrder(Long orderId, Order order){

        Order retOrder = retrieveById(orderId);
        orderMapper.copyProperties(order, retOrder);

        Set<ConstraintViolation<Order>> violations = validator.validate(retOrder);

        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);


        return orderRepository.save(retOrder);
    }

    public void deleteOrder(Long orderId){
        try {
            orderRepository.deleteById(orderId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Order does not exist");
        }

    }

}
