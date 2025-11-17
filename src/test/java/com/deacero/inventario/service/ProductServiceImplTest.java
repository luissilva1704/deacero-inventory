package com.deacero.inventario.service;

import com.deacero.inventario.entities.Product;
import com.deacero.inventario.exception.BadRequestException;
import com.deacero.inventario.exception.ConflictException;
import com.deacero.inventario.mapper.ProductMapper;
import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import com.deacero.inventario.repository.InventoryRepository;
import com.deacero.inventario.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

	private ProductRepository productRepository;
	private InventoryRepository inventoryRepository;
	private ProductMapper productMapper;

	private ProductServiceImpl service;

	@BeforeEach
	void setUp() {
		productRepository = Mockito.mock(ProductRepository.class);
		inventoryRepository = Mockito.mock(InventoryRepository.class);
		productMapper = Mappers.getMapper(ProductMapper.class);
		service = new ProductServiceImpl(productRepository, inventoryRepository, productMapper);
	}

	@Test
	@SuppressWarnings("unchecked")
	void buildSpecification_allFilters_appliesAllPredicates() throws Exception {
		var method = ProductServiceImpl.class.getDeclaredMethod("buildSpecification", String.class, BigDecimal.class, BigDecimal.class);
		method.setAccessible(true);
		Specification<Product> spec = (Specification<Product>) method.invoke(service, "Tools", new BigDecimal("5.00"), new BigDecimal("10.00"));

		Root<Product> root = mock(Root.class);
		CriteriaQuery<Product> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path categoryPath = mock(Path.class);
		Path pricePath = mock(Path.class);
		Predicate p1 = mock(Predicate.class);
		Predicate combined = mock(Predicate.class);

		when(root.get("category")).thenReturn(categoryPath);
		when(root.get("price")).thenReturn(pricePath);
		when(cb.equal(categoryPath, "Tools")).thenReturn(p1);
		when(cb.and(any(Predicate[].class))).thenReturn(combined);

		Predicate result = spec.toPredicate(root, query, cb);
		assertEquals(combined, result);
	}

	@Test
	@SuppressWarnings("unchecked")
	void buildSpecification_noFilters_nonePredicates() throws Exception {
		var method = ProductServiceImpl.class.getDeclaredMethod("buildSpecification", String.class, BigDecimal.class, BigDecimal.class);
		method.setAccessible(true);
		Specification<Product> spec = (Specification<Product>) method.invoke(service, "   ", null, null);

		Root<Product> root = mock(Root.class);
		CriteriaQuery<Product> query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path categoryPath = mock(Path.class);
		Path pricePath = mock(Path.class);
		Predicate combined = mock(Predicate.class);

		when(root.get("category")).thenReturn(categoryPath);
		when(root.get("price")).thenReturn(pricePath);
		when(cb.and(any(Predicate[].class))).thenReturn(combined);

		Predicate result = spec.toPredicate(root, query, cb);
		assertEquals(combined, result);
	}

	@Test
	void listProducts_withoutMinStock_returnsMappedPage() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
		Product p = Product.builder()
				.id(UUID.randomUUID())
				.name("Product Name")
				.category("Tools")
				.price(new BigDecimal("10.00"))
				.sku("SKU-1")
				.build();
		when(productRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(p), pageable, 1));

		Page<ProductResponse> page = service.listProducts("Tools", null, null, null, pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("Product Name", page.getContent().get(0).getName());
	}

	@Test
	void listProducts_withMinStock_filtersByInventory() {
		Pageable pageable = PageRequest.of(0, 10);
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		Product p1 = Product.builder().id(id1).name("Product Name 1").category("Product Category 1").price(new BigDecimal("1.00")).sku("Sku1").build();
		Product p2 = Product.builder().id(id2).name("Product Name 2").category("Product Category 2").price(new BigDecimal("2.00")).sku("Sku2").build();
		when(productRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));
		when(inventoryRepository.findProductIdsWithTotalQuantityAtLeast(5)).thenReturn(List.of(id2));

		Page<ProductResponse> page = service.listProducts(null, null, null, 5, pageable);

		assertEquals(1, page.getContent().size());
		assertEquals("Product Name 2", page.getContent().get(0).getName());
	}

	@Test
	void getProduct_found() {
		UUID id = UUID.randomUUID();
		Product p = Product.builder().id(id).name("Product Name").category("Product Category").price(new BigDecimal("1.00")).sku("Sku1").build();
		when(productRepository.findById(id)).thenReturn(Optional.of(p));

		Optional<ProductResponse> resp = service.getProduct(id);
		assertTrue(resp.isPresent());
		assertEquals("Product Name", resp.get().getName());
	}

	@Test
	void createProduct_validatesAndSaves() {
		ProductRequest req = ProductRequest.builder()
				.name("Product Name").category("Product Category").sku("Sku1").price(new BigDecimal("1.00"))
				.build();
		when(productRepository.findBySku("Sku1")).thenReturn(Optional.empty());
		when(productRepository.save(any(Product.class))).thenAnswer(i -> {
			Product p = i.getArgument(0);
			p.setId(UUID.randomUUID());
			return p;
		});

		ProductResponse resp = service.createProduct(req);
		assertNotNull(resp.getId());
		assertEquals("Product Name", resp.getName());
	}

	@Test
	void createProduct_rejectsMissingFields() {
		ProductRequest req = ProductRequest.builder().build();
		assertThrows(BadRequestException.class, () -> service.createProduct(req));
	}

	@Test
	void createProduct_rejectsDuplicateSku() {
		ProductRequest req = ProductRequest.builder()
				.name("Product Name").category("Product Category").sku("Sku1").price(new BigDecimal("1.00"))
				.build();
		when(productRepository.findBySku("Sku1")).thenReturn(Optional.of(new Product()));
		assertThrows(ConflictException.class, () -> service.createProduct(req));
	}

	@Test
	void updateProduct_updates_and_checksSkuConflict() {
		UUID id = UUID.randomUUID();
		Product existing = Product.builder()
				.id(id).name("Product Name").category("Product Category").price(new BigDecimal("1.00")).sku("Sku1")
				.build();
		when(productRepository.findById(id)).thenReturn(Optional.of(existing));
		when(productRepository.findBySku("Sku2")).thenReturn(Optional.of(Product.builder().id(id).build()));
		when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

		ProductRequest req = ProductRequest.builder().name("Product Name").sku("Sku2").build();
		Optional<ProductResponse> resp = service.updateProduct(id, req);

		assertTrue(resp.isPresent());
		assertEquals("Product Name", resp.get().getName());
		assertEquals("Sku2", resp.get().getSku());
	}

	@Test
	void updateProduct_throws_onOtherProductWithSameSku() {
		UUID id = UUID.randomUUID();
		Product existing = Product.builder()
				.id(id).name("Product Name").category("Product Category").price(new BigDecimal("1.00")).sku("Sku1")
				.build();
		when(productRepository.findById(id)).thenReturn(Optional.of(existing));
		when(productRepository.findBySku("Sku2")).thenReturn(Optional.of(Product.builder().id(UUID.randomUUID()).build()));

		ProductRequest req = ProductRequest.builder().sku("Sku2").build();
		assertThrows(ConflictException.class, () -> service.updateProduct(id, req));
	}
}


