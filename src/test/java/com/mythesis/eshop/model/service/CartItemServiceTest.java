package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.Cart;
import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.repository.CartItemRepository;
import com.mythesis.eshop.model.repository.CartRepository;
import com.mythesis.eshop.util.CartItemMapper;
import com.mythesis.eshop.util.CartMapper;
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
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartItemService underTest;


    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(cartItemRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        CartItem cartItem1 = underTest.retrieveById(1L);

        assertThat(cartItem1.getId()).isEqualTo(cartItem.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(cartItemRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Cart item");
    }

    @Test
    void canCreate(){

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(23);

        when(validator.validate(cartItem)).thenReturn(new HashSet<>());
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem cartItem1 = underTest.add(cartItem);

        assertThat(cartItem1.getQuantity()).isEqualTo(cartItem.getQuantity());

    }


    @Test
    void canNotAddWithViolations(){
        CartItem cartItem = new CartItem();
        when(validator.validate(cartItem).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(cartItem)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){

        CartItem cartItem = new CartItem();
        cartItem.setId(2L);

        when(validator.validate(cartItem).isEmpty()).thenReturn(true);
        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemMapper).copyProperties(cartItem, cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem cartItem1 = underTest.update(2L, cartItem);

        assertThat(cartItem1.getId()).isEqualTo(cartItem.getId());

    }


    @Test
    void canNotUpdateWithViolations(){
        CartItem cartItem = new CartItem();

        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(cartItem));
        when(validator.validate(cartItem).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(2L, cartItem)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.deleteById(1L);
        verify(cartItemRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(cartItemRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.deleteById(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Cart item does not exist");
    }

}