package com.deacero.inventario.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    @Column(name = "product_id", nullable = false)
    private java.util.UUID productId;

    @Column(name = "store_id", nullable = false, length = 50)
    private String storeId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;
}
