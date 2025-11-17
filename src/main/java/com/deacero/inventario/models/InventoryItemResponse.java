package com.deacero.inventario.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {
	private String storeId;
	private java.util.UUID productId;
	private Integer quantity;
	private Integer minStock;
}


