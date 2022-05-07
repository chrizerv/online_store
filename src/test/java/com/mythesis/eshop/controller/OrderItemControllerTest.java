package com.mythesis.eshop.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mythesis.eshop.dto.OrderItemEntryDTO;
import com.mythesis.eshop.dto.OrderItemInfoDTO;
import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.dto.UserRegisterDTO;
import com.mythesis.eshop.exception.ApiError;
import com.mythesis.eshop.model.entity.OrderItem;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.OrderItemService;
import com.mythesis.eshop.model.service.UserService;
import com.mythesis.eshop.util.OrderItemMapper;
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


@WebMvcTest(OrderItemController.class)
class OrderItemControllerTest {

    @MockBean
    private OrderItemService orderItemService;

    @MockBean
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetOrderItems_andReturn200() throws Exception {

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem());
        orderItems.add(new OrderItem());
        when(orderItemService.retrieveAll()).thenReturn(orderItems);

        mockMvc.perform(get("/orderItems"))
                .andExpect(status().isOk());

        verify(orderItemMapper).toOrderItemInfoDto(orderItems.get(0));
        verify(orderItemMapper).toOrderItemInfoDto(orderItems.get(1));

    }

    @Test
    void canNotGetOrderItems_andReturn403() throws Exception {
        mockMvc.perform(get("/orderItems"))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canGetOrderItem_andReturn200() throws Exception {
        OrderItem orderItem = new OrderItem();
        OrderItemInfoDTO expectedOrderItem = new OrderItemInfoDTO();
        expectedOrderItem.setId(2L);

        when(orderItemService.retrieveById(eq(2L))).thenReturn(orderItem);
        when(orderItemMapper.toOrderItemInfoDto(eq(orderItem))).thenReturn(expectedOrderItem);

        MvcResult mvcResult = mockMvc.perform(get("/orderItems/{orderItemId}", 2L))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedOrderItem)
        );
    }

    @Test
    void caNotOrderItem_andReturn403() throws Exception {
       mockMvc.perform(get("/orderItems/{orderItemId}", 1L))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotGetOrderItemWithWrongId_andReturn404() throws Exception {

        when(orderItemService.retrieveById(eq(2L))).thenThrow(new NoSuchElementException("No such Order Item"));
        MvcResult mvcResult  = mockMvc.perform(get("/orderItems/{orderItemId}", 2L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"No such Order Item");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
          objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canCreateOrderItem_andReturn200() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        orderItemEntry.setOrderId(1L);
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);

        when(orderItemMapper.fromOrderItemEntryDto(any())).thenReturn(orderItem);

        mockMvc.perform(post("/orderItems")
                .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<OrderItem> orderItemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        ArgumentCaptor<OrderItemEntryDTO> orderItemEntryCaptor = ArgumentCaptor.forClass(OrderItemEntryDTO.class);
        verify(orderItemMapper, times(1)).fromOrderItemEntryDto(orderItemEntryCaptor.capture());
        verify(orderItemService, times(1)).add(orderItemCaptor.capture());

        assertThat(orderItemEntryCaptor.getValue().getOrderId())
                .isEqualTo(1L);
        assertThat(orderItemCaptor.getValue().getId())
                .isEqualTo(1L);

    }

    @Test
    void canNotCreateOrderItem_andReturn403() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        orderItemEntry.setOrderId(2L);

        mockMvc.perform(post("/orderItems")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
                .andExpect(status().isForbidden());


    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotCreateOrderItemWhenViolations_andReturn400() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        Set<ConstraintViolation<OrderItem>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());


        when(orderItemService.add(any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(post("/orderItems")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
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
    void canNotCreateOrderItemWhenIllegalArgs_andReturn400() throws Exception {

        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();

        when(orderItemService.add(any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(post("/orderItems")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
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
    void canUpdateOrderItem_andReturn200() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        orderItemEntry.setOrderId(2L);
        OrderItem orderItem = new OrderItem();
        orderItem.setId(2L);

        when(orderItemMapper.fromOrderItemEntryDto(any())).thenReturn(orderItem);

        mockMvc.perform(put("/orderItems/{orderItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
                .andExpect(status().isOk());


        ArgumentCaptor<OrderItem> orderItemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<OrderItemEntryDTO> orderItemEntryCaptor = ArgumentCaptor.forClass(OrderItemEntryDTO.class);

        verify(orderItemMapper, times(1)).fromOrderItemEntryDto(orderItemEntryCaptor.capture());
        verify(orderItemService, times(1)).update(idCaptor.capture(), orderItemCaptor.capture());

        assertThat(orderItemEntryCaptor.getValue().getOrderId())
                .isEqualTo(2L);
        assertThat(orderItemCaptor.getValue().getId())
                .isEqualTo(2L);
        assertThat(idCaptor.getValue())
                .isEqualTo(1L);

    }

    @Test
    void canNotUpdateOrderItem_andReturn403() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        mockMvc.perform(put("/orderItems/{orderItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotUpdateOrderItemWhenViolations_andReturn400() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();
        Set<ConstraintViolation<OrderItem>> violations = new HashSet<>();

        List<String> expectedViolations = violations
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toList());

        when(orderItemService.update(eq(1L), any())).thenThrow(new ConstraintViolationException("Validation errors",violations));
        MvcResult mvcResult  =  mockMvc.perform(put("/orderItems/{orderItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
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
    void canNotUpdateOrderItemWhenIllegalArgs_andReturn400() throws Exception {
        OrderItemEntryDTO orderItemEntry = new OrderItemEntryDTO();

        when(orderItemService.update(eq(1L), any())).thenThrow(new IllegalArgumentException("Field already exists"));
        MvcResult mvcResult  =  mockMvc.perform(put("/orderItems/{orderItemId}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderItemEntry)))
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
    void canDeleteOrderItem_andReturn200() throws Exception {

        mockMvc.perform(delete("/orderItems/{orderItemId}", 3L))
                .andExpect(status().isOk());

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(orderItemService, times(1)).deleteById(idCaptor.capture());

        assertThat(idCaptor.getValue())
                .isEqualTo(3L);

    }

    @Test
    @WithMockUser(username = "test", roles = { "USER" })
    void canNotDeleteOrderItemWhenNotADMIN_andReturn403() throws Exception {
        mockMvc.perform(delete("/orderItems/{orderItemId}", 3L))
                .andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test", roles = { "ADMIN" })
    void canNotDeleteOrderItemWhenWrongID_andReturn404() throws Exception {

        doThrow(new NoSuchElementException("OrderItem does not exist")).when(orderItemService).deleteById(3L);
       MvcResult mvcResult = mockMvc.perform(delete("/orderItems/{orderItemId}", 3L))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError expectedErrorResponse = new ApiError(HttpStatus.NOT_FOUND.value(),"Order Item does not exist");
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedErrorResponse)
        );
    }
}