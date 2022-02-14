package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CartEntryDTO;
import com.mythesis.eshop.dto.CartInfoDTO;
import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartMapperTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private CartMapper underTest;

    @Test
    void canConvertToCartInfoDto() {
        Cart inputCart = new Cart();
        User user = new User();
        user.setId(1L);
        inputCart.setUser(user);

        when(modelMapper.map(inputCart, CartInfoDTO.class)).thenReturn(new CartInfoDTO());
        CartInfoDTO outCart = underTest.toCartInfoDto(inputCart);


        assertThat(outCart.getUserId()).isEqualTo(1L);
    }

    @Test
    void canConvertFromCartEntryDto() {
        CartEntryDTO inputCart = new CartEntryDTO();
        User user = new User();
        user.setId(1L);
        inputCart.setUserId(1L);

        when(modelMapper.map(inputCart, Cart.class)).thenReturn(new Cart());
        when(userService.retrieveById(1L)).thenReturn(user);
        Cart outCart = underTest.fromCartEntryDto(inputCart);

        assertThat(outCart.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void canConvertFromOrderEntryDtoWhenNullUser(){
        CartEntryDTO inputCart = new CartEntryDTO();

        when(modelMapper.map(inputCart, Cart.class)).thenReturn(new Cart());
        Cart outCart = underTest.fromCartEntryDto(inputCart);

        assertThat(outCart.getUser()).isEqualTo(null);
    }

    @Test
    void canCopyProperties() {
        Cart cSource = new Cart();
        Cart cDest = new Cart();
        underTest.copyProperties(cSource, cDest);

        verify(modelMapper).map(cSource, cDest);
    }
}