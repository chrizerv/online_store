package com.mythesis.eshop.model.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "e_product")
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "name must not be blank")
    private String name;
    @NotBlank(message = "description must not be blank")
    private String description;

    @Column(unique = true)
    @NotBlank(message = "sku must not be blank")
    private String sku;

    @ManyToOne
    @JoinColumn(name = "categoryId", referencedColumnName = "id")
    @NotNull(message = "category must not be blank")
    private Category category;

    @NotNull(message = "price must not be blank")
    private Double price;

    private Integer inStock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Product(){}

    public Product(String name,
                   String description,
                   String sku,
                   Category category,
                   Double price) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.category = category;
        this.price = price;
    }

    public Long getId(){ return id;}

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getInStock() {
        return inStock;
    }

    public void setInStock(Integer inStock) {
        this.inStock = inStock;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
