package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.CategoryEntryDTO;
import com.mythesis.eshop.dto.CategoryInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.service.CategoryService;
import com.mythesis.eshop.util.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;


    @Autowired
    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping("/{categoryId}")
    public CategoryInfoDTO getCategory(@PathVariable("categoryId") Long categoryId){

        return categoryMapper.toCategoryInfoDto(categoryService.retrieveById(categoryId));
    }


    @GetMapping
    public List<CategoryInfoDTO> getCategories(){
        return categoryService.retrieveAll()
                .stream()
                .map(categoryMapper::toCategoryInfoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public CategoryInfoDTO createCategory(@RequestBody CategoryEntryDTO category){
        Category mapped = categoryMapper.fromCategoryEntryDto(category);

        return categoryMapper.toCategoryInfoDto(categoryService.add(mapped));
    }


    @PutMapping("/{categoryId}")
    public CategoryInfoDTO updateCategory(@PathVariable("categoryId") Long categoryId,
                                          @RequestBody CategoryEntryDTO category){
        Category mapped = categoryMapper.fromCategoryEntryDto(category);

        return categoryMapper.toCategoryInfoDto(categoryService.update(categoryId,mapped));
    }

    @DeleteMapping("/{categoryId}")
    public void deleteCategory(@PathVariable("categoryId") Long categoryId){
        categoryService.deleteById(categoryId);
    }

}
