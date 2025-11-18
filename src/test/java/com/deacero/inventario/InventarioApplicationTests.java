package com.deacero.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.deacero.inventario.repository.ProductRepository;
import com.deacero.inventario.repository.InventoryRepository;
import com.deacero.inventario.repository.TransactionRepository;

@SpringBootTest(properties = {
		"springdoc.api-docs.enabled=false",
		"springdoc.swagger-ui.enabled=false"
})
class InventarioApplicationTests {

	@MockBean
	private ProductRepository productRepository;
	@MockBean
	private InventoryRepository inventoryRepository;
	@MockBean
	private TransactionRepository transactionRepository;

	@Test
	void contextLoads() {
	}

}
