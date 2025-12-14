package com.leang.inventoryservice.service.impl;

import com.leang.inventoryservice.exception.NotFoundException;
import com.leang.inventoryservice.model.dto.request.BatchProductRequest;
import com.leang.inventoryservice.model.dto.request.ProductRequest;
import com.leang.inventoryservice.model.dto.response.PagedResponse;
import com.leang.inventoryservice.model.dto.response.PaginationInfo;
import com.leang.inventoryservice.model.entity.Product;
import com.leang.inventoryservice.repository.ProductRepository;
import com.leang.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public PagedResponse<Product> getAllProducts(Integer page, Integer size) {
        Page<Product> products = productRepository.findAll(PageRequest.of(page - 1, size));
        return PagedResponse.<Product>builder()
                .items(products.getContent())
                .pagination(new PaginationInfo(products))
                .build();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Product with id " + id + " not found."));
    }

    @Override
    public Product updateProductById(Long id, ProductRequest productRequest) {
        Product productById = getProductById(id);
        productById.setName(productRequest.getName());
        productById.setPrice(productRequest.getPrice());
        productById.setStock(productRequest.getStock());
        return productRepository.save(productById);
    }

    @Override
    public Product createProduct(ProductRequest productRequest) {
        return productRepository.save(productRequest.toEntity());
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.delete(getProductById(id));
    }

    @Override
    public List<Product> getAllProductByIds(BatchProductRequest request) {
        List<Long> ids = request.getIds();
        List<Product> products = productRepository.findAllById(ids);
        if (products.size() != ids.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            List<Long> missingIds = request.getIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NotFoundException("Products with ids " + missingIds + " not found.");
        }
        return products;
    }
}
