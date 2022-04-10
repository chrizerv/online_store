package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Order;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.repository.OrderRepository;
import com.mythesis.eshop.model.repository.ProductRepository;
import com.mythesis.eshop.util.OrderMapper;
import com.mythesis.eshop.util.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService underTest;




    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(orderRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        User user = new User();
        Order order = new Order(user,12.4);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order order1 = underTest.retrieveById(1L);

        assertThat(order1.getId()).isEqualTo(order.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(orderRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Order");
    }

    @Test
    void canCreate(){
        User user = new User();
        Order order = new Order(user,12.4);
        order.setId(1L);

        when(validator.validate(order)).thenReturn(new HashSet<>());
        when(orderRepository.save(order)).thenReturn(order);

        Order order1 = underTest.createOrder(order);

        assertThat(order1.getId()).isEqualTo(order.getId());

    }


    @Test
    void canNotAddWithViolations(){
        User user = new User();
        Order order = new Order(user,12.4);

        when(validator.validate(order).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.createOrder(order)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){

        User user = new User();
        Order order = new Order(user,12.4);
        order.setId(1L);

        when(validator.validate(order).isEmpty()).thenReturn(true);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderMapper).copyProperties(order, order);
        when(orderRepository.save(order)).thenReturn(order);

        Order order1 = underTest.updateOrder(1L, order);

        assertThat(order1.getId()).isEqualTo(order.getId());

    }


    @Test
    void canNotUpdateWithViolations(){
        User user = new User();
        Order order = new Order(user,12.4);
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(validator.validate(order).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.updateOrder(1L, order)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.deleteOrder(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(orderRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.deleteOrder(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Order does not exist");
    }

}