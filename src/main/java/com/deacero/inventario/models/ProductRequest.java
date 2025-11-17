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
	@NotBlank
	private String description;
	@NotBlank
	private String category;
	@NotNull
	@Positive
	private BigDecimal price;

	@Pattern(regexp = "^[0-9]{8}$")
	@NotBlank
	private String sku;
}


