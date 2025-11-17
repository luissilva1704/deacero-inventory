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
	void listProducts_withoutMinStock_returnsMappedPage() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
		Product p = Product.builder()
				.id(UUID.randomUUID())
				.name("A")
				.category("Tools")
				.price(new BigDecimal("10.00"))
				.sku("SKU-1")
				.build();
		when(productRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(p), pageable, 1));

		Page<ProductResponse> page = service.listProducts("Tools", null, null, null, pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("A", page.getContent().get(0).getName());
	}

	@Test
	void listProducts_withMinStock_filtersByInventory() {
		Pageable pageable = PageRequest.of(0, 10);
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		Product p1 = Product.builder().id(id1).name("A").category("C").price(new BigDecimal("1.00")).sku("S1").build();
		Product p2 = Product.builder().id(id2).name("B").category("C").price(new BigDecimal("2.00")).sku("S2").build();
		when(productRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));
		when(inventoryRepository.findProductIdsWithTotalQuantityAtLeast(5)).thenReturn(List.of(id2));

		Page<ProductResponse> page = service.listProducts(null, null, null, 5, pageable);

		assertEquals(1, page.getContent().size());
		assertEquals("B", page.getContent().get(0).getName());
	}

	@Test
	void getProduct_found() {
		UUID id = UUID.randomUUID();
		Product p = Product.builder().id(id).name("A").category("C").price(new BigDecimal("1.00")).sku("S1").build();
		when(productRepository.findById(id)).thenReturn(Optional.of(p));

		Optional<ProductResponse> resp = service.getProduct(id);
		assertTrue(resp.isPresent());
		assertEquals("A", resp.get().getName());
	}

	@Test
	void createProduct_validatesAndSaves() {
		ProductRequest req = ProductRequest.builder()
				.name("A").category("C").sku("S1").price(new BigDecimal("1.00"))
				.build();
		when(productRepository.findBySku("S1")).thenReturn(Optional.empty());
		when(productRepository.save(any(Product.class))).thenAnswer(i -> {
			Product p = i.getArgument(0);
			p.setId(UUID.randomUUID());
			return p;
		});

		ProductResponse resp = service.createProduct(req);
		assertNotNull(resp.getId());
		assertEquals("A", resp.getName());
	}

	@Test
	void createProduct_rejectsMissingFields() {
		ProductRequest req = ProductRequest.builder().build();
		assertThrows(BadRequestException.class, () -> service.createProduct(req));
	}

	@Test
	void createProduct_rejectsDuplicateSku() {
		ProductRequest req = ProductRequest.builder()
				.name("A").category("C").sku("S1").price(new BigDecimal("1.00"))
				.build();
		when(productRepository.findBySku("S1")).thenReturn(Optional.of(new Product()));
		assertThrows(ConflictException.class, () -> service.createProduct(req));
	}

	@Test
	void updateProduct_updates_and_checksSkuConflict() {
		UUID id = UUID.randomUUID();
		Product existing = Product.builder()
				.id(id).name("A").category("C").price(new BigDecimal("1.00")).sku("S1")
				.build();
		when(productRepository.findById(id)).thenReturn(Optional.of(existing));
		when(productRepository.findBySku("S2")).thenReturn(Optional.of(Product.builder().id(id).build()));
		when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

		ProductRequest req = ProductRequest.builder().name("B").sku("S2").build();
		Optional<ProductResponse> resp = service.updateProduct(id, req);

		assertTrue(resp.isPresent());
		assertEquals("B", resp.get().getName());
		assertEquals("S2", resp.get().getSku());
	}

	@Test
	void updateProduct_throws_onOtherProductWithSameSku() {
		UUID id = UUID.randomUUID();
		Product existing = Product.builder()
				.id(id).name("A").category("C").price(new BigDecimal("1.00")).sku("S1")
				.build();
		when(productRepository.findById(id)).thenReturn(Optional.of(existing));
		when(productRepository.findBySku("S2")).thenReturn(Optional.of(Product.builder().id(UUID.randomUUID()).build()));

		ProductRequest req = ProductRequest.builder().sku("S2").build();
		assertThrows(ConflictException.class, () -> service.updateProduct(id, req));
	}

	@Test
	void deleteProduct_callsRepository() {
		UUID id = UUID.randomUUID();
		service.deleteProduct(id);
		verify(productRepository, times(1)).deleteById(id);
	}
}


