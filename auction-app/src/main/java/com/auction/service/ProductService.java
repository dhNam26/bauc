package com.auction.service;

import com.auction.model.Product;
import com.auction.model.User;
import com.auction.repository.ProductRepository;
import com.auction.util.IdGenerator;
import com.auction.util.ValidationUtil;

import java.util.List;

public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(String name, String description, User seller) {
        ValidationUtil.require(name != null && !name.isBlank(), "Product name must not be blank");
        ValidationUtil.require(seller != null, "Seller is required");
        Product product = new Product(IdGenerator.newId(), name, description, seller);
        return productRepository.save(product);
    }

    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }
}
