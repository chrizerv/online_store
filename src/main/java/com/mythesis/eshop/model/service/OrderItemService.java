package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.OrderItem;
import com.mythesis.eshop.model.repository.OrderItemRepository;
import com.mythesis.eshop.util.OrderItemMapper;
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
public class OrderItemService {

    private OrderItemRepository orderItemRepository;
    private Validator validator;
    private OrderItemMapper orderItemMapper;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository,
                            Validator validator,
                            OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.validator = validator;
        this.orderItemMapper = orderItemMapper;
    }

    public List<OrderItem> retrieveAll (){
        return  orderItemRepository.findAll();
   }

    public List<OrderItem> retrieveAllByOrderId (Long orderId){
        return  orderItemRepository.findAllByOrderId(orderId);
    }
    public OrderItem retrieveById (Long orderItemId){
        OrderItem orderItem;
        try {
            orderItem = orderItemRepository.findById(orderItemId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Order Item");
        }
        return orderItem;
    }

    public OrderItem add(OrderItem orderItem){
        Set<ConstraintViolation<OrderItem>> violations = validator.validate(orderItem);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return orderItemRepository.save(orderItem);
    }

    public OrderItem update(Long orderItemId, OrderItem orderItem){

        OrderItem retrieved = retrieveById(orderItemId);
        orderItemMapper.copyProperties(orderItem, retrieved);

        Set<ConstraintViolation<OrderItem>> violations = validator.validate(retrieved);

        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);


        return orderItemRepository.save(retrieved);
    }


    public void deleteById(Long orderItemId){
        try {
            orderItemRepository.deleteById(orderItemId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Order item does not exist");
        }
    }

}
