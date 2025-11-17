package com.deacero.inventario.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class StockLoadRequest {
	@NotNull
	private java.util.UUID productId;
	@NotBlank
	private String storeId;
	@NotNull
	@Min(0)
	private Integer quantity;
	@Min(0)
	private Integer minStock;
}


