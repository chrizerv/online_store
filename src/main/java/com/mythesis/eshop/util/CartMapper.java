package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CartEntryDTO;
import com.mythesis.eshop.dto.CartInfoDTO;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {

    private ModelMapper modelMapper;
    private UserService userService;

    @Autowired
    public CartMapper(ModelMapper modelMapper, UserService userService) {
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    public CartInfoDTO toCartInfoDto(Cart cart){
        CartInfoDTO mapped = modelMapper.map(cart, CartInfoDTO.class);
        mapped.setUserId(cart.getUser().getId());
        return mapped;
    }


    public Cart fromCartEntryDto(CartEntryDTO cart){
        Cart mappedCart = modelMapper.map(cart, Cart.class);

        if (cart.getUserId() != null){
            User user = userService.retrieveById(cart.getUserId());
            mappedCart.setUser(user);

        }
        return mappedCart;
    }

    public void copyProperties(Cart source, Cart destination){
        modelMapper.map(source,destination);

    }


}
