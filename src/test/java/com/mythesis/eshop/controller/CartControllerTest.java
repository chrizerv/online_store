package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.CartEntryDTO;
import com.mythesis.eshop.dto.CartInfoDTO;
import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.CartItemInfoDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.service.CartItemService;
import com.mythesis.eshop.model.service.CartService;
import com.mythesis.eshop.util.CartItemMapper;
import com.mythesis.eshop.util.CartMapper;
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


@WebMvcTest(CartController.class)
class CartControllerTest {

    @MockBean
    private CartService cartService;

    @MockBean
    private CartMapper cartMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCarts_andReturn200() throws Exception {

        List<Cart> carts = new ArrayList<>();
        carts.add(new Cart());
        carts.add(new Cart());
        when(cartService.retrieveAll()).thenReturn(carts);

        mockMvc.perform(get("/carts"))
                .andExpect(status().isOk());

        verify(cartMapper).toCartInfoDto(carts.get(0));
        verify(cartMapper).toCartInfoDto(carts.get(1));

    }

    @Test
    void canNotGetCarts_andReturn403() throws Exception {
        mockMvc.perform(get("/carts"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCart_andReturn200() throws Exception {
        Cart cart = new Cart();
        CartInfoDTO expectedCart = new CartInfoDTO();
        expectedCart.setId(4L);

        when(cartService.retrieveById(eq(1L))).thenReturn(cart);
        when(cartMapper.toCartInfoDto(eq(cart))).thenReturn(expectedCart);

        MvcResult mvcResult = mockMvc.perform(get("/carts/{cartId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedCart)
        );
    }

    @Test
    void caNotGetCart_andReturn403() throws Exception {
       mockMvc.perform(get("/carts/{cartId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetCartWithWrongId_andReturn404() throws Exception {

        when(cartService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Cart"));
        MvcResult mvcResult  = mockMvc.perform(get("/carts/{cartId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Cart");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateCart_andReturn200() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        cartEntry.setTotal(20.0);
        Cart cart = new Cart();
        cart.setTotal(20.0);

        when(cartMapper.fromCartEntryDto(any())).thenReturn(cart);

        mockMvc.perform(post("/carts")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        ArgumentCaptor<CartEntryDTO> cartEntryCaptor = ArgumentCaptor.forClass(CartEntryDTO.class);
        verify(cartMapper, times(1)).fromCartEntryDto(cartEntryCaptor.capture());
        verify(cartService, times(1)).add(cartCaptor.capture());

        assertThat(cartEntryCaptor.getValue().getTotal())
                .isEqualTo(20.0);
        assertThat(cartCaptor.getValue().getTotal())
                .isEqualTo(20.0);

    }

    @Test
    void canNotCreateCart_andReturn403() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        cartEntry.setTotal(20.0);

        mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateCartWhenViolations_andReturn400() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(cartService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(),expectedViolations);
        expectedErrorResponse.setMessage("Fields validation error");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateCartWhenIllegalArgs_andReturn400() throws Exception {

        CartEntryDTO cartEntry = new CartEntryDTO();

        when(cartService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/carts")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(), "Field already exists");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canUpdateCart_andReturn200() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        cartEntry.setTotal(20.0);
        Cart cart = new Cart();
        cart.setTotal(20.0);

        when(cartMapper.fromCartEntryDto(any())).thenReturn(cart);

        mockMvc.perform(put("/carts/{cartId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CartEntryDTO> cartEntryCaptor = ArgumentCaptor.forClass(CartEntryDTO.class);

        verify(cartMapper, times(1)).fromCartEntryDto(cartEntryCaptor.capture());
        verify(cartService, times(1)).update(idCaptor.capture(), cartCaptor.capture());

        assertThat(cartEntryCaptor.getValue().getTotal())
                .isEqualTo(20.0);
        assertThat(cartCaptor.getValue().getTotal())
                .isEqualTo(20.0);
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateCart_andReturn403() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        mockMvc.perform(put("/carts/{cartId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateCartWhenViolations_andReturn400() throws Exception {
        CartEntryDTO cartEntry = new CartEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(cartService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/carts/{cartId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartEntry)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(),expectedViolations);
        expectedErrorResponse.setMessage("Fields validation error");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateCartWhenIllegalArgs_andReturn400() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();

        when(cartService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/carts/{cartId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.BAD_REQUEST.value(), "Field already exists");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canDeleteCart_andReturn200() throws Exception {

        mockMvc.perform(delete("/carts/{cartId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(cartService, times(1)).deleteById(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteCartWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/carts/{cartId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteCartWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("Cart does not exist")).when(cartService).deleteById(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/carts/{cartId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Cart does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}