package com.deacero.inventario.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
	private java.util.UUID id;
	private String name;
	private String description;
	private String category;
	private BigDecimal price;
	private String sku;
}


