package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.OrderEntryDTO;
import com.mythesis.eshop.dto.OrderInfoDTO;
import com.mythesis.eshop.dto.ProductEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.Product;
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
class OrderMapperTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private OrderMapper underTest;

    @Test
    void canConvertToOrderInfoDto() {
        Order inputOrder = new Order();
        User user = new User();
        user.setId(1L);
        inputOrder.setUser(user);

        when(modelMapper.map(inputOrder, OrderInfoDTO.class)).thenReturn(new OrderInfoDTO());
        OrderInfoDTO outOrder = underTest.toOrderInfoDto(inputOrder);


        assertThat(outOrder.getUserId()).isEqualTo(1L);
    }

    @Test
    void canConvertFromOrderEntryDto() {
        OrderEntryDTO inputOrder = new OrderEntryDTO();
        User user = new User();
        user.setId(1L);
        inputOrder.setUserId(1L);

        when(modelMapper.map(inputOrder, Order.class)).thenReturn(new Order());
        when(userService.retrieveById(1L)).thenReturn(user);
        Order outOrder = underTest.fromOrderEntryDto(inputOrder);

        assertThat(outOrder.getUser().getId()).isEqualTo(1L);
    }

    @Test
    void canConvertFromOrderEntryDtoWhenNullUser(){
        OrderEntryDTO inputOrder = new OrderEntryDTO();

        when(modelMapper.map(inputOrder, Order.class)).thenReturn(new Order());
        Order outOrder = underTest.fromOrderEntryDto(inputOrder);

        assertThat(outOrder.getUser()).isEqualTo(null);
    }

    @Test
    void copyProperties() {
        Order oSource = new Order();
        Order oDest = new Order();
        underTest.copyProperties(oSource, oDest);

        verify(modelMapper).map(oSource, oDest);
    }
}