package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.CartItemInfoDTO;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.service.CartItemService;
import com.mythesis.eshop.util.CartItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/cartItems")
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartItemMapper cartItemMapper;

    @Autowired
    public CartItemController(CartItemService cartItemService, CartItemMapper cartItemMapper) {
        this.cartItemService = cartItemService;
        this.cartItemMapper = cartItemMapper;
    }

    @GetMapping("/{cartItemId}")
    public CartItemInfoDTO getCartItem(@PathVariable("cartItemId") Long cartItemId){
        return cartItemMapper.toCartItemInfoDto(cartItemService.retrieveById(cartItemId));
    }

    @GetMapping
    public List<CartItemInfoDTO> getCartItems(){
        return cartItemService.retrieveAll()
                .stream()
                .map(cartItemMapper::toCartItemInfoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public CartItemInfoDTO createCartItem(@RequestBody CartItemEntryDTO cartItem){
        CartItem mapped = cartItemMapper.fromCartItemEntryDto(cartItem);

        return cartItemMapper.toCartItemInfoDto(cartItemService.add(mapped));
    }

    @PutMapping("/{cartItemId}")
    public CartItemInfoDTO updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                   @RequestBody CartItemEntryDTO cartItem){
        CartItem mapped = cartItemMapper.fromCartItemEntryDto(cartItem);

        return cartItemMapper.toCartItemInfoDto(cartItemService.update(cartItemId,mapped));
    }

    @DeleteMapping("/{cartItemId}")
    public void deleteCartItem(@PathVariable("cartItemId") Long cartItemId){
        cartItemService.deleteById(cartItemId);
    }

}
