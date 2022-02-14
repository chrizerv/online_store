package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CategoryEntryDTO;
import com.mythesis.eshop.dto.CategoryInfoDTO;
import com.mythesis.eshop.dto.UserInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryMapper underTest;

    @Test
    void canConvertToCategoryInfoDto() {
        Category category = new Category();
        underTest.toCategoryInfoDto(category);

        verify(modelMapper).map(category, CategoryInfoDTO.class);

    }

    @Test
    void canConvertFromCategoryEntryDto() {
        CategoryEntryDTO category = new CategoryEntryDTO();
        underTest.fromCategoryEntryDto(category);

        verify(modelMapper).map(category, Category.class);
    }

    @Test
    void copyProperties() {
        Category cSource = new Category();
        Category cDest = new Category();
        underTest.copyProperties(cSource, cDest);

        verify(modelMapper).map(cSource, cDest);
    }
}