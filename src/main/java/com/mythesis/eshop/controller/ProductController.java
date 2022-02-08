package com.mythesis.eshop.controller;

import com.mythesis.eshop.dto.ProductEntryDTO;
import com.mythesis.eshop.dto.ProductInfoDTO;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.service.ProductService;
import com.mythesis.eshop.util.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/products")
public class ProductController {


    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping(path = "/{productId}")
    public ProductInfoDTO getProduct(@PathVariable("productId") Long productId){
        return productMapper.toProductInfoDto(productService.retrieveById(productId));
    }

    @GetMapping
    public List<ProductInfoDTO> getProducts(){

        return productService.retrieveAll()
                .stream()
                .map(productMapper::toProductInfoDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ProductInfoDTO createProduct(@RequestBody ProductEntryDTO product){
        Product pro = productMapper.fromProductEntryDto(product);
        Product savedPro = productService.add(pro);
        return productMapper.toProductInfoDto(savedPro);
    }

    @PutMapping(path = "/{productId}")
    public ProductInfoDTO updateProduct(@PathVariable("productId") Long productId,
                                 @RequestBody ProductEntryDTO product){
        Product pro = productMapper.fromProductEntryDto(product);
        Product updatePro = productService.update(productId,pro);

        return productMapper.toProductInfoDto(updatePro);
    }

    @DeleteMapping(path = "/{productId}")
    public void deleteProduct(@PathVariable("productId") Long productId){
        productService.delete(productId);
    }
}
