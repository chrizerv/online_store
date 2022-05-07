package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.CartItemEntryDTO;
import com.mythesis.eshop.dto.CartItemInfoDTO;
import com.mythesis.eshop.dto.CategoryEntryDTO;
import com.mythesis.eshop.dto.CategoryInfoDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.service.CartItemService;
import com.mythesis.eshop.model.service.CategoryService;
import com.mythesis.eshop.util.CartItemMapper;
import com.mythesis.eshop.util.CategoryMapper;
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


@WebMvcTest(CartItemController.class)
class CartItemControllerTest {

    @MockBean
    private CartItemService cartItemService;

    @MockBean
    private CartItemMapper cartItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCartItems_andReturn200() throws Exception {

        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem());
        cartItems.add(new CartItem());
        when(cartItemService.retrieveAll()).thenReturn(cartItems);

        mockMvc.perform(get("/cartItems"))
                .andExpect(status().isOk());

        verify(cartItemMapper).toCartItemInfoDto(cartItems.get(0));
        verify(cartItemMapper).toCartItemInfoDto(cartItems.get(1));

    }

    @Test
    void canNotGetCartItems_andReturn403() throws Exception {
        mockMvc.perform(get("/cartItems"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetCartItem_andReturn200() throws Exception {
        CartItem cartItem = new CartItem();
        CartItemInfoDTO expectedCartItem = new CartItemInfoDTO();
        expectedCartItem.setId(4L);

        when(cartItemService.retrieveById(eq(1L))).thenReturn(cartItem);
        when(cartItemMapper.toCartItemInfoDto(eq(cartItem))).thenReturn(expectedCartItem);

        MvcResult mvcResult = mockMvc.perform(get("/cartItems/{cartItemId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedCartItem)
        );
    }

    @Test
    void caNotGetCartItem_andReturn403() throws Exception {
       mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetCartItemWithWrongId_andReturn404() throws Exception {

        when(cartItemService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Cart Item"));
        MvcResult mvcResult  = mockMvc.perform(get("/cartItems/{cartItemId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Cart Item");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateCartItem_andReturn200() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        cartItemEntry.setCartId(4L);
        CartItem cartItem = new CartItem();
        cartItem.setId(4L);

        when(cartItemMapper.fromCartItemEntryDto(any())).thenReturn(cartItem);

        mockMvc.perform(post("/cartItems")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        ArgumentCaptor<CartItemEntryDTO> cartItemEntryCaptor = ArgumentCaptor.forClass(CartItemEntryDTO.class);
        verify(cartItemMapper, times(1)).fromCartItemEntryDto(cartItemEntryCaptor.capture());
        verify(cartItemService, times(1)).add(cartItemCaptor.capture());

        assertThat(cartItemEntryCaptor.getValue().getCartId())
                .isEqualTo(4L);
        assertThat(cartItemCaptor.getValue().getId())
                .isEqualTo(4L);

    }

    @Test
    void canNotCreateCartItem_andReturn403() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        cartItemEntry.setCartId(4L);

        mockMvc.perform(post("/cartItems")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateCartItemWhenViolations_andReturn400() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(cartItemService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/cartItems")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
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
    void canNotCreateCartItemWhenIllegalArgs_andReturn400() throws Exception {

        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();

        when(cartItemService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/cartItems")
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
    @WithMockUser(username = "test", roles = { "USER" })
    void canUpdateCartItem_andReturn200() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        cartItemEntry.setCartId(4L);
        CartItem cartItem = new CartItem();
        cartItem.setId(4L);

        when(cartItemMapper.fromCartItemEntryDto(any())).thenReturn(cartItem);

        mockMvc.perform(put("/cartItems/{cartItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<CartItemEntryDTO> categoryEntryCaptor = ArgumentCaptor.forClass(CartItemEntryDTO.class);

        verify(cartItemMapper, times(1)).fromCartItemEntryDto(categoryEntryCaptor.capture());
        verify(cartItemService, times(1)).update(idCaptor.capture(), cartItemCaptor.capture());

        assertThat(categoryEntryCaptor.getValue().getCartId())
                .isEqualTo(4L);
        assertThat(cartItemCaptor.getValue().getId())
                .isEqualTo(4L);
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateCartItem_andReturn403() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        mockMvc.perform(put("/cartItems/{cartItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateCartItemWhenViolations_andReturn400() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(cartItemService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/cartItems/{cartItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cartItemEntry)))
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
    void canNotUpdateCartItemWhenIllegalArgs_andReturn400() throws Exception {
        CartItemEntryDTO cartItemEntry = new CartItemEntryDTO();

        when(cartItemService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/cartItems/{cartItemId}", 1L)
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
    void canDeleteCartItem_andReturn200() throws Exception {

        mockMvc.perform(delete("/cartItems/{cartItemId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(cartItemService, times(1)).deleteById(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteCartItemWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/cartItems/{cartItemId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteCartItemWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("Cart Item does not exist")).when(cartItemService).deleteById(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/cartItems/{cartItemId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Cart Item does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}