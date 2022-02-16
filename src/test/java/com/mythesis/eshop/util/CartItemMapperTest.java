package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.CartItemInfoDTO;
import com.mythesis.eshop.dto.OrderItemEntryDTO;
import com.mythesis.eshop.dto.OrderItemInfoDTO;
import com.mythesis.eshop.model.entity.*;
import com.mythesis.eshop.model.service.CartService;
import com.mythesis.eshop.model.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartItemMapperTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CartService cartService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private CartItemMapper underTest;


    @Test
    void canConvertToCartItemInfoDto() {
        CartItem inputCartItem = new CartItem();
        Cart cart = new Cart();
        Product product = new Product();
        cart.setId(1L);
        product.setId(2L);
        inputCartItem.setCart(cart);
        inputCartItem.setProduct(product);

        when(modelMapper.map(inputCartItem, CartItemInfoDTO.class)).thenReturn(new CartItemInfoDTO());
        CartItemInfoDTO outCartItem = underTest.toCartItemInfoDto(inputCartItem);

        assertThat(outCartItem.getCartId()).isEqualTo(1l);
        assertThat(outCartItem.getProductId()).isEqualTo(2L);

    }

    @Test
    void canConvertFromCartItemEntryDto() {
        CartItemEntryDTO inputCartItem = new CartItemEntryDTO();
        inputCartItem.setCartId(1L);
        inputCartItem.setProductId(2L);
        Cart cart = new Cart();
        Product product = new Product();

        when(modelMapper.map(inputCartItem, CartItem.class)).thenReturn(new CartItem());
        when(cartService.retrieveById(1L)).thenReturn(cart);
        when(productService.retrieveById(2L)).thenReturn(product);
        CartItem outCartItem = underTest.fromCartItemEntryDto(inputCartItem);

        assertThat(outCartItem.getCart()).isEqualTo(cart);
        assertThat(outCartItem.getProduct()).isEqualTo(product);
    }

    @Test
    void canConvertFromCartItemEntryDtoWhenNullCart(){
        CartItemEntryDTO inputCartItem = new CartItemEntryDTO();
        inputCartItem.setProductId(2L);

        Product product = new Product();

        when(modelMapper.map(inputCartItem, CartItem.class)).thenReturn(new CartItem());

        when(productService.retrieveById(2L)).thenReturn(product);
        CartItem outCartItem = underTest.fromCartItemEntryDto(inputCartItem);

        assertThat(outCartItem.getCart()).isEqualTo(null);
        assertThat(outCartItem.getProduct()).isEqualTo(product);
    }

    @Test
    void canConvertFromCartItemEntryDtoWhenNullProduct(){
        CartItemEntryDTO inputCartItem = new CartItemEntryDTO();
        inputCartItem.setCartId(1L);

        Cart cart = new Cart();


        when(modelMapper.map(inputCartItem, CartItem.class)).thenReturn(new CartItem());
        when(cartService.retrieveById(1L)).thenReturn(cart);

        CartItem outCartItem = underTest.fromCartItemEntryDto(inputCartItem);

        assertThat(outCartItem.getCart()).isEqualTo(cart);
        assertThat(outCartItem.getProduct()).isEqualTo(null);
    }

    @Test
    void canCopyProperties() {
        CartItem cSource = new CartItem();
        CartItem cDest = new CartItem();
        underTest.copyProperties(cSource, cDest);

        verify(modelMapper).map(cSource, cDest);
    }
}