package com.klu.ecommerce_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klu.ecommerce_backend.model.BackgroundJob;

public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, Long> {

}