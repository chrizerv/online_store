package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.model.service.ProductService;
import com.mythesis.eshop.util.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public UserProductController(ProductService productService,
                                 ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping(path = "/users/{userId}/ordered-products")
    public List<ProductInfoDTO> getProductsOrderedByUser(@PathVariable("userId")Long userId){
        return productService.retrieveAllOrderedByUser(userId)
                .stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());

    }

    @GetMapping(path = "/users/{userId}/cart-products")
    public List<ProductInfoDTO> getInCartProductsByUser(@PathVariable("userId")Long userId){
        return productService.retrieveAllInCartByUser(userId)
                .stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }
}
