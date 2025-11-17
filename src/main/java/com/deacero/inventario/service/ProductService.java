package com.deacero.inventario.service;

import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
	Page<ProductResponse> listProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock, Pageable pageable);
	Optional<ProductResponse> getProduct(UUID id);
	ProductResponse createProduct(ProductRequest product);
	Optional<ProductResponse> updateProduct(UUID id, ProductRequest product);
	void deleteProduct(UUID id);
}


