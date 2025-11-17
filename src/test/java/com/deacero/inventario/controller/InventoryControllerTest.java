package com.deacero.inventario.controller;

import com.deacero.inventario.models.*;
import com.deacero.inventario.service.InventoryService;
import com.deacero.inventario.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@Import(InventoryControllerTest.TestConfig.class)
class InventoryControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ProductService productService;

	@Autowired
	private InventoryService inventoryService;

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		ProductService productService() {
			return Mockito.mock(ProductService.class);
		}
		@Bean
		@Primary
		InventoryService inventoryService() {
			return Mockito.mock(InventoryService.class);
		}
	}

	@Test
	void listProducts_ok() throws Exception {
		Page<ProductResponse> page = new PageImpl<>(List.of(ProductResponse.builder()
				.id(UUID.randomUUID()).name("A").category("C").price(new BigDecimal("1.00")).sku("S1").build()));
		Mockito.when(productService.listProducts(any(), any(), any(), any(), any())).thenReturn(page);

		mvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)))
				.andExpect(jsonPath("$.data.content[0].name", is("A")));
	}

	@Test
	void getProduct_found() throws Exception {
		UUID id = UUID.randomUUID();
		Mockito.when(productService.getProduct(id)).thenReturn(Optional.of(ProductResponse.builder().id(id).name("A").build()));

		mvc.perform(get("/api/products/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name", is("A")));
	}

	@Test
	void getProduct_notFound() throws Exception {
		UUID id = UUID.randomUUID();
		Mockito.when(productService.getProduct(id)).thenReturn(Optional.empty());

		mvc.perform(get("/api/products/" + id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.code", is("NOT_FOUND")));
	}

	@Test
	void createProduct_created() throws Exception {
		ProductResponse resp = ProductResponse.builder().id(UUID.randomUUID()).name("A").build();
		Mockito.when(productService.createProduct(any())).thenReturn(resp);

		mvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"A\",\"category\":\"C\",\"price\":10.0,\"sku\":\"S1\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name", is("A")));
	}

	@Test
	void updateProduct_ok() throws Exception {
		UUID id = UUID.randomUUID();
		ProductResponse resp = ProductResponse.builder().id(id).name("B").build();
		Mockito.when(productService.updateProduct(eq(id), any())).thenReturn(Optional.of(resp));

		mvc.perform(put("/api/products/" + id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"B\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name", is("B")));
	}

	@Test
	void deleteProduct_ok() throws Exception {
		UUID id = UUID.randomUUID();
		mvc.perform(delete("/api/products/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)));
	}

	@Test
	void inventoryByStore_ok() throws Exception {
		Mockito.when(inventoryService.getInventoryByStore("S1"))
				.thenReturn(List.of(InventoryItemResponse.builder().storeId("S1").productId(UUID.randomUUID()).quantity(1).minStock(0).build()));

		mvc.perform(get("/api/stores/S1/inventory"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].storeId", is("S1")));
	}

	@Test
	void transfer_ok() throws Exception {
		mvc.perform(post("/api/inventory/transfer")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"sourceStoreId\":\"A\",\"targetStoreId\":\"B\",\"quantity\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)));
	}

	@Test
	void alerts_ok() throws Exception {
		Mockito.when(inventoryService.listLowStockAlerts())
				.thenReturn(List.of(LowStockProductResponse.builder().productName("X").build()));

		mvc.perform(get("/api/inventory/alerts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].productName", is("X")));
	}

	@Test
	void load_in_out_history_ok() throws Exception {
		// load
		mvc.perform(post("/api/inventory/load")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"storeId\":\"S1\",\"quantity\":10,\"minStock\":1}"))
				.andExpect(status().isCreated());
		// in
		mvc.perform(post("/api/inventory/in")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"storeId\":\"S1\",\"quantity\":5}"))
				.andExpect(status().isCreated());
		// out
		mvc.perform(post("/api/inventory/out")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"productId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"storeId\":\"S1\",\"quantity\":2}"))
				.andExpect(status().isCreated());
		// history
		Page<TransactionResponse> page = new PageImpl<>(List.of(TransactionResponse.builder().build()));
		Mockito.when(inventoryService.listHistory(any(), any(), any())).thenReturn(page);
		mvc.perform(get("/api/inventory/history"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success", is(true)));
	}
}


