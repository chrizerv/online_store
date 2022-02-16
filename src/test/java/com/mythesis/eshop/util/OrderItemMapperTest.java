package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.*;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.OrderItem;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.OrderService;
import com.mythesis.eshop.model.service.ProductService;
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
class OrderItemMapperTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ProductService productService;
    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderItemMapper underTest;

    @Test
    void canConvertToOrderItemInfoDto() {
        OrderItem inputOrderItem = new OrderItem();
        Order order = new Order();
        Product product = new Product();
        order.setId(1L);
        product.setId(2L);
        inputOrderItem.setOrder(order);
        inputOrderItem.setProduct(product);

        when(modelMapper.map(inputOrderItem, OrderItemInfoDTO.class)).thenReturn(new OrderItemInfoDTO());
        OrderItemInfoDTO outOrderItem = underTest.toOrderItemInfoDto(inputOrderItem);

        assertThat(outOrderItem.getOrderId()).isEqualTo(1L);
        assertThat(outOrderItem.getProductId()).isEqualTo(2L);
    }

    @Test
    void canConvertFromOrderItemEntryDto() {
        OrderItemEntryDTO inputOrderItem = new OrderItemEntryDTO();
        inputOrderItem.setOrderId(1L);
        inputOrderItem.setProductId(2L);
        Order order = new Order();
        Product product = new Product();

        when(modelMapper.map(inputOrderItem, OrderItem.class)).thenReturn(new OrderItem());
        when(orderService.retrieveById(1L)).thenReturn(order);
        when(productService.retrieveById(2L)).thenReturn(product);
        OrderItem outOrderItem = underTest.fromOrderItemEntryDto(inputOrderItem);

        assertThat(outOrderItem.getOrder()).isEqualTo(order);
        assertThat(outOrderItem.getProduct()).isEqualTo(product);
    }

    @Test
    void canConvertFromOrderItemEntryDtoWhenNullOrder() {
        OrderItemEntryDTO inputOrderItem = new OrderItemEntryDTO();
        inputOrderItem.setProductId(2L);

        Product product = new Product();

        when(modelMapper.map(inputOrderItem, OrderItem.class)).thenReturn(new OrderItem());

        when(productService.retrieveById(2L)).thenReturn(product);
        OrderItem outOrderItem = underTest.fromOrderItemEntryDto(inputOrderItem);

        assertThat(outOrderItem.getOrder()).isEqualTo(null);
        assertThat(outOrderItem.getProduct()).isEqualTo(product);
    }

    @Test
    void canConvertFromOrderItemEntryDtoWhenNullProduct() {
        OrderItemEntryDTO inputOrderItem = new OrderItemEntryDTO();
        inputOrderItem.setOrderId(1L);

        Order order = new Order();


        when(modelMapper.map(inputOrderItem, OrderItem.class)).thenReturn(new OrderItem());
        when(orderService.retrieveById(1L)).thenReturn(order);

        OrderItem outOrderItem = underTest.fromOrderItemEntryDto(inputOrderItem);

        assertThat(outOrderItem.getOrder()).isEqualTo(order);
        assertThat(outOrderItem.getProduct()).isEqualTo(null);
    }

    @Test
    void canCopyProperties() {
        OrderItem oSource = new OrderItem();
        OrderItem oDest = new OrderItem();
        underTest.copyProperties(oSource, oDest);

        verify(modelMapper).map(oSource, oDest);
    }
}