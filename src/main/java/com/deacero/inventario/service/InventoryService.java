package com.deacero.inventario.service;

import com.deacero.inventario.models.InventoryItemResponse;
import com.deacero.inventario.models.LowStockProductResponse;
import com.deacero.inventario.models.MovementRequest;
import com.deacero.inventario.models.StockLoadRequest;
import com.deacero.inventario.models.TransactionResponse;
import com.deacero.inventario.models.TransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InventoryService {
	List<InventoryItemResponse> getInventoryByStore(String storeId);
	void transfer(TransferRequest request);
	List<LowStockProductResponse> listLowStockAlerts();
	void loadInitialStock(StockLoadRequest request);
	void registerEntry(MovementRequest request);
	void registerOut(MovementRequest request);
	Page<TransactionResponse> listHistory(UUID productId, String storeId, Pageable pageable);
}


