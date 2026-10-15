package com.klu.ecommerce_backend.repository;
import com.klu.ecommerce_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository
        extends JpaRepository<Product, Long> {
}

