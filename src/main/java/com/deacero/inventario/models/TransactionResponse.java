package com.deacero.inventario.models;

import com.deacero.inventario.entities.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
	private UUID id;
	private UUID productId;
	private String sourceStoreId;
	private String targetStoreId;
	private Integer quantity;
	private OffsetDateTime timestamp;
	private Transaction.Type type;
}


