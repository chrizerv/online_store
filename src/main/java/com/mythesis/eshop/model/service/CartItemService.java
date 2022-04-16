package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.repository.CartItemRepository;
import com.mythesis.eshop.util.CartItemMapper;
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
public class CartItemService {

    private CartItemRepository cartItemRepository;
    private CartItemMapper cartItemMapper;
    private Validator validator;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository,
                           CartItemMapper cartItemMapper,
                           Validator validator) {
        this.cartItemRepository = cartItemRepository;
        this.cartItemMapper = cartItemMapper;
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
}
