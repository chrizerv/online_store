package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.CategoryEntryDTO;
import com.mythesis.eshop.dto.CategoryInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    private ModelMapper modelMapper;

    @Autowired
    public CategoryMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CategoryInfoDTO toCategoryInfoDto(Category category){
        CategoryInfoDTO mapped = modelMapper.map(category, CategoryInfoDTO.class);

        return mapped;
    }


    public Category fromCategoryEntryDto(CategoryEntryDTO category){
        Category mapped = modelMapper.map(category, Category.class);
        return mapped;
    }

    public void copyProperties(Category source, Category destination){
        modelMapper.map(source,destination);

    }
}
