package com.deacero.inventario.service;

import com.deacero.inventario.entities.Product;
import com.deacero.inventario.mapper.ProductMapper;
import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import com.deacero.inventario.repository.InventoryRepository;
import com.deacero.inventario.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import com.deacero.inventario.exception.BadRequestException;
import com.deacero.inventario.exception.ConflictException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;
	private final ProductMapper productMapper;

	public ProductServiceImpl(ProductRepository productRepository, InventoryRepository inventoryRepository, ProductMapper productMapper) {
		this.productRepository = productRepository;
		this.inventoryRepository = inventoryRepository;
		this.productMapper = productMapper;
	}

	@Override
	public Page<ProductResponse> listProducts(String category, BigDecimal minPrice, BigDecimal maxPrice, Integer minStock, Pageable pageable) {
		Specification<Product> spec = buildSpecification(category, minPrice, maxPrice);
		Page<Product> basePage = productRepository.findAll(spec, pageable);

		if (minStock == null) {
			return productMapper.toResponsePage(basePage, pageable);
		}

		// Filter by minimum stock by intersecting with productIds that meet min stock
		List<UUID> productIdsWithStock = inventoryRepository.findProductIdsWithTotalQuantityAtLeast(minStock);
		if (productIdsWithStock.isEmpty()) {
			return Page.empty(pageable);
		}

		List<Product> filtered = basePage.getContent().stream()
				.filter(p -> productIdsWithStock.contains(p.getId()))
				.toList();

		Page<Product> filteredPage = new PageImpl<>(filtered, pageable, basePage.getTotalElements());
		return productMapper.toResponsePage(filteredPage, pageable);
	}

	private Specification<Product> buildSpecification(String category, BigDecimal minPrice, BigDecimal maxPrice) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (category != null && !category.isBlank()) {
				predicates.add(cb.equal(root.get("category"), category));
			}
			if (minPrice != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
			}
			if (maxPrice != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
			}
			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	@Override
	public Optional<ProductResponse> getProduct(UUID id) {
		return productRepository.findById(id).map(productMapper::toResponse);
	}

	@Override
	@Transactional
	public ProductResponse createProduct(ProductRequest product) {
		productRepository.findBySku(product.getSku()).ifPresent(p -> {
			throw new ConflictException("SKU already exists");
		});
		Product toSave = productMapper.toEntity(product);
		Product saved = productRepository.save(toSave);
		return productMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public Optional<ProductResponse> updateProduct(UUID id, ProductRequest product) {
		return productRepository.findById(id).map(existing -> {
			if (product.getSku() != null) {
				productRepository.findBySku(product.getSku()).ifPresent(other -> {
					if (!other.getId().equals(id)) {
						throw new ConflictException("SKU already exists");
					}
				});
			}
			productMapper.updateEntityFromRequest(product, existing);
			Product saved = productRepository.save(existing);
			return productMapper.toResponse(saved);
		});
	}

	@Override
	@Transactional
	public void deleteProduct(UUID id) {
		productRepository.deleteById(id);
	}
}


