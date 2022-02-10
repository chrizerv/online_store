package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.ProductEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.entity.User;
import com.mythesis.eshop.model.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductMapperTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductMapper underTest;

    @Test
    void canConvertToProductInfoDto() {
        Product inputProduct = new Product();
        inputProduct.setCategory(new Category("testCategory"));

        when(modelMapper.map(inputProduct, ProductInfoDTO.class)).thenReturn(new ProductInfoDTO());
        ProductInfoDTO outProduct = underTest.toProductInfoDto(inputProduct);


        assertThat(outProduct.getCategoryName()).isEqualTo("testCategory");
    }

    @Test
    void canConvertFromProductEntryDto() {
        ProductEntryDTO inputProduct = new ProductEntryDTO();
        inputProduct.setCategoryId(1L);

        when(modelMapper.map(inputProduct, Product.class)).thenReturn(new Product());
        when(categoryService.retrieveById(1L)).thenReturn(new Category("testCategory"));
        Product outProduct = underTest.fromProductEntryDto(inputProduct);

        assertThat(outProduct.getCategory().getTitle()).isEqualTo("testCategory");

    }
    @Test
    void canConvertFromProductEntryWhenNullCategory() {
        ProductEntryDTO inputProduct = new ProductEntryDTO();
        inputProduct.setCategoryId(null);

        when(modelMapper.map(inputProduct, Product.class)).thenReturn(new Product());
        Product outProduct = underTest.fromProductEntryDto(inputProduct);

        assertThat(outProduct.getCategory()).isEqualTo(null);

    }

    @Test
    void canCopyProperties() {

        Product pSource = new Product();
        Product pDest = new Product();
        underTest.copyProperties(pSource, pDest);

        verify(modelMapper).map(pSource, pDest);
    }
}