package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.repository.CartRepository;
import com.mythesis.eshop.util.CartMapper;
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
public class CartService {

    private CartRepository cartRepository;
    private Validator validator;
    private CartMapper cartMapper;

    @Autowired
    public CartService(CartRepository cartRepository,
                       Validator validator,
                       CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.validator = validator;
        this.cartMapper = cartMapper;
    }

    public List<Cart> retrieveAll (){
        return cartRepository.findAll();
    }

    public List<Cart> retrieveAllByUserId (Long userId){
        return cartRepository.findAllByUserId(userId);
    }

    public Cart retrieveById (Long cartId){
        Cart cart;
        try {
            cart = cartRepository.findById(cartId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Cart");
        }
        return  cart;
    }

    public Cart add(Cart cart){

        Set<ConstraintViolation<Cart>> violations = validator.validate(cart);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return cartRepository.save(cart);
    }

    public Cart update(Long cartId, Cart cart){

        Cart retrieved = retrieveById(cartId);
        cartMapper.copyProperties(cart, retrieved);

        Set<ConstraintViolation<Cart>> violations = validator.validate(retrieved);

        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return cartRepository.save(retrieved);
    }

    public void deleteById(Long cartId){
        try {
            cartRepository.deleteById(cartId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Cart does not exist");
        }
    }
}
