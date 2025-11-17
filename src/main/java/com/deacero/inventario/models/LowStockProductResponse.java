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
public class LowStockProductResponse {
    private java.util.UUID productId;
    private String productName;
    private String sku;
    private BigDecimal price;
    private String storeId;
    private Integer quantity;
    private Integer minStock;
}


