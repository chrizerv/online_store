package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.CartEntryDTO;
import com.mythesis.eshop.dto.CartInfoDTO;
import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.CartService;
import com.mythesis.eshop.model.service.ProductService;
import com.mythesis.eshop.util.CartMapper;
import com.mythesis.eshop.util.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserProductController.class)
class UserProductControllerTest {

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetProductsOrderedByUser_andReturn200() throws Exception {

        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());
        when(productService.retrieveAllOrderedByUser(1L)).thenReturn(products);

        mockMvc.perform(get("/users/{userId}/ordered-products", 1L))
                .andExpect(status().isOk());

        verify(productMapper).toProductInfoDto(products.get(0));
        verify(productMapper).toProductInfoDto(products.get(1));

    }

    @Test
    void canNotGetProductsOrderedByUser_andReturn403() throws Exception {
        mockMvc.perform(get("/users/{userId}/ordered-products", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetInCartProductsByUser_andReturn200() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());
        when(productService.retrieveAllInCartByUser(1L)).thenReturn(products);

        mockMvc.perform(get("/users/{userId}/cart-products", 1L))
                .andExpect(status().isOk());

        verify(productMapper).toProductInfoDto(products.get(0));
        verify(productMapper).toProductInfoDto(products.get(1));
    }

    @Test
    void caNotGetInCartProductsByUser_andReturn403() throws Exception {
       mockMvc.perform(get("/users/{userId}/cart-products", 1L))
                .andExpect(status().isForbidden());

    }


}