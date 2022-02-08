package com.mythesis.eshop.util;

import com.mythesis.eshop.dto.ProductEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.model.entity.Category;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private ModelMapper modelMapper;
    private CategoryService categoryService;

    @Autowired
    public ProductMapper(ModelMapper modelMapper,
                         CategoryService categoryService) {
        this.modelMapper = modelMapper;
        this.categoryService = categoryService;
    }

    public ProductInfoDTO toProductInfoDto(Product product){
        ProductInfoDTO mapped = modelMapper.map(product, ProductInfoDTO.class);
        mapped.setCategoryName(product.getCategory().getTitle());
        return  mapped;
    }


    public Product fromProductEntryDto(ProductEntryDTO product){
        Product  mappedProduct = modelMapper.map(product, Product.class);
        if (product.getCategoryId() != null) {
            Category category = categoryService.retrieveById(product.getCategoryId());
            mappedProduct.setCategory(category);
        }

        return mappedProduct;
    }

    public void copyProperties(Product source, Product destination){
        modelMapper.map(source,destination);
    }
}
