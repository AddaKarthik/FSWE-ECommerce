package com.klu.ecommerce_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klu.ecommerce_backend.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}