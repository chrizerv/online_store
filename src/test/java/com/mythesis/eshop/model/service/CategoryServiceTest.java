package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.*;
import com.mythesis.eshop.model.repository.CategoryRepository;
import com.mythesis.eshop.model.repository.OrderItemRepository;
import com.mythesis.eshop.util.CategoryMapper;
import com.mythesis.eshop.util.OrderItemMapper;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService underTest;


    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(categoryRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        Category cat = new Category("Electronics");
        cat.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));

        Category cat1 = underTest.retrieveById(1L);

        assertThat(cat1.getId()).isEqualTo(cat1.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(categoryRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Category");
    }

    @Test
    void canCreate(){
        Category cat = new Category("Electronics");


        when(validator.validate(cat)).thenReturn(new HashSet<>());
        when(categoryRepository.save(cat)).thenReturn(cat);

        Category cat1 = underTest.add(cat);

        assertThat(cat1.getTitle()).isEqualTo(cat.getTitle());

    }


    @Test
    void canNotAddWithViolations(){
        Category cat = new Category("Electronics");
        when(validator.validate(cat).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(cat)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canUpdate(){
        Category cat = new Category("Electronics");
        cat.setId(2L);

        when(validator.validate(cat).isEmpty()).thenReturn(true);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        doNothing().when(categoryMapper).copyProperties(cat, cat);
        when(categoryRepository.save(cat)).thenReturn(cat);

        Category cat1 = underTest.update(2L, cat);

        assertThat(cat1.getId()).isEqualTo(2L);

    }


    @Test
    void canNotUpdateWithViolations(){
        Category cat = new Category("Electronics");
        cat.setId(2L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(validator.validate(cat).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(2L, cat)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.deleteById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(categoryRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.deleteById(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category does not exist");
    }

}