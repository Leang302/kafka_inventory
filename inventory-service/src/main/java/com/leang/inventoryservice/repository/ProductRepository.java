package com.leang.inventoryservice.repository;

import com.leang.inventoryservice.model.entity.Product;
import com.leang.inventoryservice.service.impl.ProductServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
}
