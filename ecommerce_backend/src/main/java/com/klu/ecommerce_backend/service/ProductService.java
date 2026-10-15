package com.klu.ecommerce_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.klu.ecommerce_backend.model.Product;
import com.klu.ecommerce_backend.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add Product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Get All Products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get Product By ID
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    // Update Product
    public Product updateProduct(Long productId, Product product) {

        Product existingProduct =
                productRepository.findById(productId).orElse(null);

        if (existingProduct == null) {
            return null;
        }

        existingProduct.setProductName(product.getProductName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStock(product.getStock());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setRating(product.getRating());
        existingProduct.setStatus(product.getStatus());

        return productRepository.save(existingProduct);
    }

    // Delete Product
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}