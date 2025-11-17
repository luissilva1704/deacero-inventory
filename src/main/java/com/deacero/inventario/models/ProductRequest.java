package com.deacero.inventario.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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
public class ProductRequest {
	@NotBlank
	private String name;
	// description is optional
	private String description;
	@NotBlank
	private String category;
	@NotNull
	@Positive
	private BigDecimal price;

	@NotBlank
	private String sku;
}


