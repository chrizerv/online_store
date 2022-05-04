package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.OrderService;
import com.mythesis.eshop.model.service.UserService;
import com.mythesis.eshop.util.OrderMapper;
import com.mythesis.eshop.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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


@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetOrders_andReturn200() throws Exception {

        List<Order> orders = new ArrayList<>();
        orders.add(new Order());
        orders.add(new Order());
        when(orderService.retrieveAll()).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());

        verify(orderMapper).toOrderInfoDto(orders.get(0));
        verify(orderMapper).toOrderInfoDto(orders.get(1));

    }

    @Test
    void canNotGetOrders_andReturn403() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetOrder_andReturn200() throws Exception {
        Order order = new Order();
        OrderInfoDTO expectedOrder = new OrderInfoDTO();
        expectedOrder.setTotal(40.0);

        when(orderService.retrieveById(eq(1L))).thenReturn(order);
        when(orderMapper.toOrderInfoDto(eq(order))).thenReturn(expectedOrder);

        MvcResult mvcResult = mockMvc.perform(get("/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedOrder)
        );
    }

    @Test
    void caNotGetOrder_andReturn403() throws Exception {
       mockMvc.perform(get("/orders/{orderId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetOrderWithWrongId_andReturn404() throws Exception {

        when(orderService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Order"));
        MvcResult mvcResult  = mockMvc.perform(get("/orders/{orderId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Order");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateOrder_andReturn200() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        orderEntry.setTotal(40.0);
        Order order = new Order();
        order.setTotal(40.0);

        when(orderMapper.fromOrderEntryDto(any())).thenReturn(order);

        mockMvc.perform(post("/orders")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<OrderEntryDTO> orderEntryCaptor = ArgumentCaptor.forClass(OrderEntryDTO.class);
        verify(orderMapper, times(1)).fromOrderEntryDto(orderEntryCaptor.capture());
        verify(orderService, times(1)).createOrder(orderCaptor.capture());

        assertThat(orderEntryCaptor.getValue().getTotal())
                .isEqualTo(40.0);
        assertThat(orderCaptor.getValue().getTotal())
                .isEqualTo(40.0);

    }

    @Test
    void canNotCreateOrder_andReturn403() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        orderEntry.setTotal(40.0);

        mockMvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateOrderWhenViolations_andReturn400() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(orderService.createOrder(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
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
    void canNotCreateOrderWhenIllegalArgs_andReturn400() throws Exception {

        OrderEntryDTO orderEntry = new OrderEntryDTO();

        when(orderService.createOrder(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
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
    void canUpdateOrder_andReturn200() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        orderEntry.setTotal(40.0);
        Order order = new Order();
        order.setTotal(40.0);

        when(orderMapper.fromOrderEntryDto(any())).thenReturn(order);

        mockMvc.perform(put("/orders/{orderId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<OrderEntryDTO> orderEntryCaptor = ArgumentCaptor.forClass(OrderEntryDTO.class);

        verify(orderMapper, times(1)).fromOrderEntryDto(orderEntryCaptor.capture());
        verify(orderService, times(1)).updateOrder(idCaptor.capture(), orderCaptor.capture());

        assertThat(orderEntryCaptor.getValue().getTotal())
                .isEqualTo(40.0);
        assertThat(orderCaptor.getValue().getTotal())
                .isEqualTo(40.0);
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateOrder_andReturn403() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        mockMvc.perform(put("/orders/{orderId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateOrderWhenViolations_andReturn400() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();
        Set<ConstraintViolation<Order>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(orderService.updateOrder(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/orders/{orderId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
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
    void canNotUpdateOrderWhenIllegalArgs_andReturn400() throws Exception {
        OrderEntryDTO orderEntry = new OrderEntryDTO();

        when(orderService.updateOrder(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/orders/{orderId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderEntry)))
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
    void canDeleteOrder_andReturn200() throws Exception {

        mockMvc.perform(delete("/orders/{orderId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(orderService, times(1)).deleteOrder(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteOrderWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/orders/{orderId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteOrderWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("Order does not exist")).when(orderService).deleteOrder(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/orders/{orderId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Order does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}