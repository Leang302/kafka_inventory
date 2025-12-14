package com.leang.inventoryservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "products")
@Table
public class Product extends BaseEntityAudit{
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer stock;
    private BigDecimal price;
}
