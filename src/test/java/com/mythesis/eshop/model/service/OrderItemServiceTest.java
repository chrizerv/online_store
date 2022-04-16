package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.*;
import com.mythesis.eshop.model.repository.OrderItemRepository;
import com.mythesis.eshop.model.repository.OrderRepository;
import com.mythesis.eshop.util.OrderItemMapper;
import com.mythesis.eshop.util.OrderMapper;
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
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService underTest;


    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(orderItemRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        Category cat = new Category("Electronics");

        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);

        User user = new User();
        Order order = new Order(user,12.4);

        OrderItem orderItem = new OrderItem(order,product);

        orderItem.setId(1L);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));

        OrderItem orderItem1 = underTest.retrieveById(1L);

        assertThat(orderItem1.getId()).isEqualTo(orderItem.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(orderItemRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Order Item");
    }

    @Test
    void canCreate(){
        Category cat = new Category("Electronics");
        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);
        User user = new User();
        product.setId(1L);
        Order order = new Order(user,12.4);
        order.setId(2L);
        OrderItem orderItem = new OrderItem(order,product);

        when(validator.validate(orderItem)).thenReturn(new HashSet<>());
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);

        OrderItem orderItem1 = underTest.add(orderItem);

        assertThat(orderItem1.getProduct().getId()).isEqualTo(1L);
        assertThat(orderItem1.getOrder().getId()).isEqualTo(2L);

    }


    @Test
    void canNotAddWithViolations(){
        Category cat = new Category("Electronics");
        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);
        User user = new User();
        Order order = new Order(user,12.4);
        OrderItem orderItem = new OrderItem(order,product);

        when(validator.validate(orderItem).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(orderItem)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){
        Category cat = new Category("Electronics");
        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);
        User user = new User();
        product.setId(1L);
        Order order = new Order(user,12.4);
        order.setId(2L);
        OrderItem orderItem = new OrderItem(order,product);
        orderItem.setId(4L);

        when(validator.validate(orderItem).isEmpty()).thenReturn(true);
        when(orderItemRepository.findById(4L)).thenReturn(Optional.of(orderItem));
        doNothing().when(orderItemMapper).copyProperties(orderItem, orderItem);
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);

        OrderItem orderItem1 = underTest.update(4L, orderItem);

        assertThat(orderItem1.getId()).isEqualTo(4L);

    }


    @Test
    void canNotUpdateWithViolations(){
        Category cat = new Category("Electronics");
        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);
        User user = new User();
        product.setId(1L);
        Order order = new Order(user,12.4);
        order.setId(2L);
        OrderItem orderItem = new OrderItem(order,product);
        orderItem.setId(4L);


        when(orderItemRepository.findById(4L)).thenReturn(Optional.of(orderItem));
        when(validator.validate(orderItem).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(4L, orderItem)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.deleteById(1L);
        verify(orderItemRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(orderItemRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.deleteById(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Order item does not exist");
    }

}