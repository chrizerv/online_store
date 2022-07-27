package com.mythesis.eshop.model.service;

import com.mythesis.eshop.model.entity.CartItem;
import com.mythesis.eshop.model.entity.Product;
import com.mythesis.eshop.model.repository.ProductRepository;
import com.mythesis.eshop.util.ProductMapper;
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
public class ProductService {

    private ProductRepository productRepository;
    private Validator validator;
    private ProductMapper productMapper;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          Validator validator,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.validator = validator;
        this.productMapper = productMapper;
    }

    public List<Product> retrieveAll(){
        return productRepository.findAll();
    }

    public List<Product> retrieveAllOrderedByUser(Long userId){
        return productRepository.findAllOrderedByUserId(userId);
    }
    public List<Product> retrieveAllInCartByUser(Long userId){
        return productRepository.findAllInCartByUserId(userId);
    }
    public Product retrieveById (Long productId){

        Product product;
        try {
            product = productRepository.findById(productId).get();
        } catch (NoSuchElementException ex){
            throw new NoSuchElementException("No such Product");
        }
        return product;
    }

    public Product add (Product product){

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        if (skuExists(product.getSku()))
            throw new IllegalArgumentException("SKU already exists");


        Product savedProduct = productRepository.save(product);

        return savedProduct;
    }

    public Product update (Long productId, Product product){

        if (skuExists(product.getSku()))
            throw new IllegalArgumentException("SKU already exists");

        Product retrievedProd = retrieveById(productId);
        productMapper.copyProperties(product, retrievedProd);

        Set<ConstraintViolation<Product>> violations = validator.validate(retrievedProd);
        if (!violations.isEmpty())
            throw new ConstraintViolationException("Validation errors",violations);

        Product updatedProduct = productRepository.save(retrievedProd);

        return updatedProduct;
    }
    public void delete (Long productId){
        try {
            productRepository.deleteById(productId);
        }catch (EmptyResultDataAccessException ex){
            throw new NoSuchElementException("Product does not exist");
        }
    }

    public boolean skuExists(String sku){
        return productRepository.findBySku(sku).isPresent();
    }

    public Double getTotalPriceOfProducts(List<CartItem> cartItems) {
        Double totalPrice = 0.0;
        for (CartItem ci : cartItems) {
            Integer quantity = ci.getQuantity();
            Long productId = ci.getProduct().getId();
            Double productPrice = productRepository.findById(productId).get().getPrice();
            totalPrice += (productPrice * quantity);
        }
        return totalPrice;
    }
    public void updateProductStock(List<CartItem> cartItems) {

        for ( CartItem entry : cartItems ){
            Product product = this.retrieveById(entry.getProduct().getId());
            if (product.getInStock() < entry.getQuantity()){
                throw new IllegalStateException("Product Not In stock");
                //productAvailable = false;
            }
            product.setInStock(product.getInStock() - entry.getQuantity());

            productRepository.save(product);
        }

    }

    public List<Product> getProductsOrderedAndInCartByUser(Long userId) {
        return productRepository.findProductsOrderedAndInCartByUser(userId);
    }
}
