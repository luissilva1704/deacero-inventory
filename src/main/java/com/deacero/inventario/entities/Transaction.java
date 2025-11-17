package com.deacero.inventario.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private java.util.UUID id;

    @Column(name = "product_id", nullable = false)
    private java.util.UUID productId;

    @Column(name = "source_store_id", length = 50)
    private String sourceStoreId;

    @Column(name = "target_store_id", length = 50)
    private String targetStoreId;

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "\"timestamp\"", nullable = false)
    private OffsetDateTime timestamp;

    public enum Type {
        IN,
        OUT,
        TRANSFER
    }

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "transaction_type", nullable = false)
    private Type type;
}
