package com.klu.ecommerce_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klu.ecommerce_backend.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}