package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.*;
import com.mythesis.eshop.model.repository.CartItemRepository;
import com.mythesis.eshop.util.CartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class CartItemService {

    private CartItemRepository cartItemRepository;
    private CartItemMapper cartItemMapper;
    private Validator validator;
    private CartService cartService;
    private OrderService orderService;
    private PaymentService paymentService;
    private ShippingService shippingService;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository,
                           CartItemMapper cartItemMapper,
                           OrderService orderService,
                           CartService cartService,
                           PaymentService paymentService,
                           ShippingService shippingService,
                           Validator validator) {
        this.cartItemRepository = cartItemRepository;
        this.cartItemMapper = cartItemMapper;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.validator = validator;
    }

    public List<CartItem> retrieveAll (){
        return  cartItemRepository.findAll();
    }

    public List<CartItem> retrieveAllByCartId (Long cartId){
        return  cartItemRepository.findAllByCartId(cartId);
    }
    public CartItem retrieveById (Long cartItemId){
        CartItem cartItem;
        try {
            cartItem = cartItemRepository.findById(cartItemId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Cart item");
        }
        return  cartItem;

    }

    public CartItem add(CartItem cartItem){

        Set<ConstraintViolation<CartItem>> violations = validator.validate(cartItem);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return cartItemRepository.save(cartItem);

    }

    public CartItem update(Long cartItemId, CartItem cartItem){
        CartItem retrieved = retrieveById(cartItemId);
        cartItemMapper.copyProperties(cartItem, retrieved);

        Set<ConstraintViolation<CartItem>> violations = validator.validate(retrieved);

        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return cartItemRepository.save(retrieved);
    }

    public void deleteById(Long cartItemId){
        try {
            cartItemRepository.deleteById(cartItemId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Cart item does not exist");
        }
    }

    /* The order of the transactions does not matter. All of the must be completed or not regardless which is first, second etc.*/
    @Transactional
    public Boolean purchaseAllInCartItems(Long cartId){
        User user = cartService.retrieveById(cartId).getUser();
        List<CartItem> cartItemList = this.retrieveAllByCartId(cartId);

        Order order = new Order();
        order.setUser(user);

        List<OrderItem> orderItemList = new ArrayList<>();
        Double total = 0.0;
        for (CartItem ci : cartItemList) {

            orderItemList.add(
                    new OrderItem(order, ci.getProduct())
            );
            total += ci.getProduct().getPrice();
        }
        order.setTotal(total);
        order.setOrderItems(orderItemList);

        orderService.createOrder(order);
        paymentService.pay(user.getId(), total);
        shippingService.ship(order);

        return true;
    }
}
