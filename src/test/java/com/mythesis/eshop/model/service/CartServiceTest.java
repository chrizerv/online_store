package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.repository.CartRepository;
import com.mythesis.eshop.model.repository.CategoryRepository;
import com.mythesis.eshop.util.CartMapper;
import com.mythesis.eshop.util.CategoryMapper;
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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService underTest;


    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(cartRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        Cart cart = new Cart();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

        Cart cart1 = underTest.retrieveById(1L);

        assertThat(cart1.getId()).isEqualTo(cart.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(cartRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Cart");
    }

    @Test
    void canCreate(){

        Cart cart = new Cart();
        cart.setTotal(22.0);

        when(validator.validate(cart)).thenReturn(new HashSet<>());
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart cart1 = underTest.add(cart);

        assertThat(cart1.getTotal()).isEqualTo(cart.getTotal());

    }


    @Test
    void canNotAddWithViolations(){
        Cart cart = new Cart();
        when(validator.validate(cart).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(cart)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){

        Cart cart = new Cart();
        cart.setId(2L);

        when(validator.validate(cart).isEmpty()).thenReturn(true);
        when(cartRepository.findById(2L)).thenReturn(Optional.of(cart));
        doNothing().when(cartMapper).copyProperties(cart, cart);
        when(cartRepository.save(cart)).thenReturn(cart);

        Cart cart1 = underTest.update(2L, cart);

        assertThat(cart1.getId()).isEqualTo(cart.getId());

    }


    @Test
    void canNotUpdateWithViolations(){
        Cart cart = new Cart();

        when(cartRepository.findById(2L)).thenReturn(Optional.of(cart));
        when(validator.validate(cart).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(2L, cart)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.deleteById(1L);
        verify(cartRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(cartRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.deleteById(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Cart does not exist");
    }

}