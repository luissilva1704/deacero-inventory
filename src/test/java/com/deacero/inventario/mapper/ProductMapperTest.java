package com.deacero.inventario.mapper;

import com.deacero.inventario.entities.Product;
import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductMapperTest {

	private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

	@Test
	void toEntity_mapsAllFields() {
		ProductRequest req = ProductRequest.builder()
				.name("Product Name").description("Product Description").category("Product Category").price(new BigDecimal("1.23")).sku("S1")
				.build();

		Product entity = mapper.toEntity(req);
		assertEquals("Product Name", entity.getName());
		assertEquals("Product Description", entity.getDescription());
		assertEquals("Product Category", entity.getCategory());
		assertEquals(new BigDecimal("1.23"), entity.getPrice());
		assertEquals("S1", entity.getSku());
	}

	@Test
	void toResponse_mapsAllFields() {
		UUID id = UUID.randomUUID();
		Product entity = Product.builder()
				.id(id).name("Product Name").description("Product Description").category("Product Category").price(new BigDecimal("2.34")).sku("S2")
				.build();

		ProductResponse resp = mapper.toResponse(entity);
		assertEquals(id, resp.getId());
		assertEquals("Product Name", resp.getName());
		assertEquals("Product Description", resp.getDescription());
		assertEquals("Product Category", resp.getCategory());
		assertEquals(new BigDecimal("2.34"), resp.getPrice());
		assertEquals("S2", resp.getSku());
	}

	@Test
	void updateEntityFromRequest_ignoresNulls_and_updatesNonNulls() {
		Product entity = Product.builder()
				.id(UUID.randomUUID()).name("Old Product Name").description("Old Product Description").category("Old Product Category").price(new BigDecimal("5.00")).sku("OLD")
				.build();
		ProductRequest req = ProductRequest.builder()
				.name("New Product Name").description(null).category("New Product Category").price(new BigDecimal("10.00")).sku("New Product Sku")
				.build();

		mapper.updateEntityFromRequest(req, entity);

		assertEquals("New Product Name", entity.getName());
		assertEquals("Old Product Description", entity.getDescription());
		assertEquals("New Product Category", entity.getCategory());
		assertEquals(new BigDecimal("10.00"), entity.getPrice());
		assertEquals("New Product Sku", entity.getSku());
	}

	@Test
	void toResponsePage_mapsPageContent_and_metadata() {
		Pageable pageable = PageRequest.of(0, 2);
		List<Product> content = List.of(
				Product.builder().id(UUID.randomUUID()).name("Product A").price(new BigDecimal("1.00")).sku("S1").build(),
				Product.builder().id(UUID.randomUUID()).name("Product B").price(new BigDecimal("2.00")).sku("S2").build()
		);
		Page<Product> page = new PageImpl<>(content, pageable, 2);

		Page<ProductResponse> mapped = mapper.toResponsePage(page, pageable);
		assertEquals(2, mapped.getTotalElements());
		assertEquals(2, mapped.getContent().size());
		assertNotNull(mapped.getContent().get(0).getName());
	}
}


