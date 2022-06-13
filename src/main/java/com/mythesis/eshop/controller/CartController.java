package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.CartEntryDTO;
import com.mythesis.eshop.dto.CartInfoDTO;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.service.CartItemService;
import com.mythesis.eshop.model.service.CartService;
import com.mythesis.eshop.util.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/carts")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final CartMapper cartMapper;

    @Autowired
    public CartController(CartService cartService, CartItemService cartItemService, CartMapper cartMapper) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.cartMapper = cartMapper;
    }

    @GetMapping("/{cartId}")
    public CartInfoDTO getCart(@PathVariable("cartId") Long cartId){
        return cartMapper.toCartInfoDto(cartService.retrieveById(cartId));
    }

    @GetMapping("/{cartId}/purchase")
    public Boolean purchaseAllInCartItems(@PathVariable("cartId") Long cartId){
        return cartItemService.purchaseAllInCartItems(cartId);
    }

    @GetMapping
    public List<CartInfoDTO> getCarts(){
        return cartService.retrieveAll()
                .stream()
                .map(cartMapper::toCartInfoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public CartInfoDTO createCart(@RequestBody CartEntryDTO cart){
        Cart mapped = cartMapper.fromCartEntryDto(cart);
        return cartMapper.toCartInfoDto(cartService.add(mapped));
    }

    @PutMapping("/{cartId}")
    public CartInfoDTO updateCart(@PathVariable("cartId") Long cartId,
                                  @RequestBody CartEntryDTO cart){

        Cart mapped = cartMapper.fromCartEntryDto(cart);
        return cartMapper.toCartInfoDto(cartService.update(cartId, mapped));

    }

    @DeleteMapping("/{cartId}")
    public void deleteCart(@PathVariable("cartId") Long cartId){
        cartService.deleteById(cartId);
    }

}
