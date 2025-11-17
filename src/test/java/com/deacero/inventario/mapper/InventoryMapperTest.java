package com.deacero.inventario.mapper;

import com.deacero.inventario.entities.Inventory;
import com.deacero.inventario.models.InventoryItemResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryMapperTest {

	private final InventoryMapper mapper = Mappers.getMapper(InventoryMapper.class);

	@Test
	void toItemResponse_mapsAllFields() {
		Inventory inv = Inventory.builder()
				.id(UUID.randomUUID())
				.productId(UUID.randomUUID())
				.storeId("S1")
				.quantity(10)
				.minStock(2)
				.build();

		InventoryItemResponse resp = mapper.toItemResponse(inv);
		assertEquals(inv.getProductId(), resp.getProductId());
		assertEquals("S1", resp.getStoreId());
		assertEquals(10, resp.getQuantity());
		assertEquals(2, resp.getMinStock());
	}
}


