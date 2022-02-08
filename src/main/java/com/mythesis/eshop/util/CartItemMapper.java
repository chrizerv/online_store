package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.CartItemInfoDTO;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.CartService;
import com.mythesis.eshop.model.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    private ModelMapper modelMapper;
    private CartService cartService;
    private ProductService productService;


    @Autowired
    public CartItemMapper(ModelMapper modelMapper,
                          CartService cartService,
                          ProductService productService) {
        this.modelMapper = modelMapper;
        this.cartService = cartService;
        this.productService = productService;
    }

    public CartItemInfoDTO toCartItemInfoDto(CartItem cartItem){
        CartItemInfoDTO mapped = modelMapper.map(cartItem, CartItemInfoDTO.class);
        mapped.setCartId(cartItem.getCart().getId());
        mapped.setProductId(cartItem.getProduct().getId());
        return mapped;
    }


    public CartItem fromCartItemEntryDto(CartItemEntryDTO cartItem){
        CartItem mappedCartItem = modelMapper.map(cartItem, CartItem.class);

        if (cartItem.getCartId() != null){
            Cart cart = cartService.retrieveById(cartItem.getCartId());
            mappedCartItem.setCart(cart);

        }

        if (cartItem.getProductId() != null){
            Product product = productService.retrieveById(cartItem.getProductId());
            mappedCartItem.setProduct(product);

        }
        return mappedCartItem;
    }

    public void copyProperties(CartItem source, CartItem destination){
        modelMapper.map(source,destination);

    }



}
