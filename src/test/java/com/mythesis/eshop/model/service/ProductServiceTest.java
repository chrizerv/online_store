package com.mythesis.eshop.model.service;


import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.repository.ProductRepository;
import com.mythesis.eshop.model.repository.UserRepository;
import com.mythesis.eshop.util.ProductMapper;
import com.mythesis.eshop.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Validator validator;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService underTest;




    @Test
    void canRetrieveAll(){
        underTest.retrieveAll();
        verify(productRepository).findAll();
    }

    @Test
    void canRetrieveById(){
        Category cat = new Category("Electronics");

        Product product = new Product();
        product.setId(1L);
        product.setCategory(cat);
        product.setPrice(10.3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product product1 = underTest.retrieveById(1L);

        assertThat(product1.getId()).isEqualTo(product.getId());

    }

    @Test
    void canNotRetrieveByNonExistingId(){
        when(productRepository.findById(55L)).thenThrow(NoSuchElementException.class);

        assertThatThrownBy(
                () -> underTest.retrieveById(55L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("No such Product");
    }

    @Test
    void canAdd(){
        Category cat = new Category("Electronics");

        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);

        when(validator.validate(product)).thenReturn(new HashSet<>());
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.empty());
        when(productRepository.save(product)).thenReturn(product);

        Product product1 = underTest.add(product);

        assertThat(product1.getSku()).isEqualTo(product.getSku());

    }

    @Test
    void canNotAddExistingSku(){
        Category cat = new Category("Electronics");

        Product product = new Product(
                "test prod",
                "test test",
                "311234",
                cat, 10.2);

        when(validator.validate(product)).thenReturn(new HashSet<>());
        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(product));

        assertThatThrownBy(
                ()-> underTest.add(product)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SKU already exists");

    }

    @Test
    void canNotAddWithViolations(){
        Product product = new Product();

        when(validator.validate(product).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.add(product)
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
        product.setId(1L);

        when(validator.validate(product).isEmpty()).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).copyProperties(product, product);
        when(productRepository.save(product)).thenReturn(product);

        Product product1 = underTest.update(1L, product);

        assertThat(product1.getId()).isEqualTo(product.getId());

    }



    @Test
    void canNotUpdateWithExistingSku(){
        Product product = new Product();
        product.setSku("23531");

        when(productRepository.findBySku(product.getSku())).thenReturn(Optional.of(product));

        assertThatThrownBy(
                ()-> underTest.update(1L, product)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SKU already exists");

    }

    @Test
    void canNotUpdateWithViolations(){
        Product product = new Product();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(validator.validate(product).isEmpty()).thenReturn(false);

        assertThatThrownBy(
                ()-> underTest.update(1L, product)
        ).isInstanceOf(ConstraintViolationException.class)
                .hasMessage("Validation errors");
    }

    @Test
    void canDelete(){
        underTest.delete(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void canNotDelete(){
        doThrow(EmptyResultDataAccessException.class).when(productRepository).deleteById(1L);
        assertThatThrownBy(
                ()-> underTest.delete(1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("Product does not exist");
    }

    @Test
    void canCheckSku(){
        underTest.skuExists("1234");
        verify(productRepository).findBySku("1234");
    }

}