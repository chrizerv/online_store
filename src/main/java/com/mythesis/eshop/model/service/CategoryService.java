package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.repository.CategoryRepository;
import com.mythesis.eshop.util.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class CategoryService {

    private CategoryRepository categoryRepository;
    private Validator validator;
    private CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           Validator validator,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.validator = validator;
        this.categoryMapper = categoryMapper;
    }

    public List<Category> retrieveAll (){
       return  categoryRepository.findAll();
    }

    public Category retrieveById (Long categoryId){

        Category category;
        try {
            category = categoryRepository.findById(categoryId).get();
        }catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Category");
        }
        return category;

    }

    public Category add(Category category){

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return categoryRepository.save(category);
    }

    public Category update(Long categoryId, Category category){

        Category retrieved = retrieveById(categoryId);
        categoryMapper.copyProperties(category, retrieved);

        Set<ConstraintViolation<Category>> violations = validator.validate(retrieved);

        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        return categoryRepository.save(retrieved);
    }

    public void deleteById(Long categoryId){
        try {
            categoryRepository.deleteById(categoryId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Category does not exist");
        }
    }
}
