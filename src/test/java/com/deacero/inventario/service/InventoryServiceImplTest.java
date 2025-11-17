package com.deacero.inventario.service;

import com.deacero.inventario.entities.Inventory;
import com.deacero.inventario.entities.Product;
import com.deacero.inventario.entities.Transaction;
import com.deacero.inventario.exception.ConflictException;
import com.deacero.inventario.exception.InsufficientStockException;
import com.deacero.inventario.mapper.InventoryMapper;
import com.deacero.inventario.models.InventoryItemResponse;
import com.deacero.inventario.models.LowStockProductResponse;
import com.deacero.inventario.models.MovementRequest;
import com.deacero.inventario.models.StockLoadRequest;
import com.deacero.inventario.models.TransactionResponse;
import com.deacero.inventario.models.TransferRequest;
import com.deacero.inventario.repository.InventoryRepository;
import com.deacero.inventario.repository.ProductRepository;
import com.deacero.inventario.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryServiceImplTest {

	private InventoryRepository inventoryRepository;
	private TransactionRepository transactionRepository;
	private ProductRepository productRepository;
	private InventoryMapper inventoryMapper;

	private InventoryServiceImpl service;

	@BeforeEach
	void setUp() {
		inventoryRepository = Mockito.mock(InventoryRepository.class);
		transactionRepository = Mockito.mock(TransactionRepository.class);
		productRepository = Mockito.mock(ProductRepository.class);
		inventoryMapper = inv -> InventoryItemResponse.builder()
				.productId(inv.getProductId())
				.storeId(inv.getStoreId())
				.quantity(inv.getQuantity())
				.minStock(inv.getMinStock())
				.build();
		service = new InventoryServiceImpl(inventoryRepository, transactionRepository, productRepository, inventoryMapper);
	}

	@Test
	void getInventoryByStore_mapsItems() {
		UUID pid = UUID.randomUUID();
		when(inventoryRepository.findByStoreId("S1"))
				.thenReturn(List.of(Inventory.builder().productId(pid).storeId("S1").quantity(3).minStock(1).build()));

		List<InventoryItemResponse> out = service.getInventoryByStore("S1");
		assertEquals(1, out.size());
		assertEquals(pid, out.get(0).getProductId());
	}

	@Test
	void transfer_happyPath_savesTwoInventoriesAndTransaction() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("A", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("A").productId(productId).quantity(10).minStock(0).build()));
		when(inventoryRepository.findByStoreIdAndProductId("B", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("B").productId(productId).quantity(2).minStock(0).build()));

		service.transfer(TransferRequest.builder()
				.productId(productId)
				.sourceStoreId("A")
				.targetStoreId("B")
				.quantity(3)
				.build());

		verify(inventoryRepository, times(2)).save(any(Inventory.class));
		verify(transactionRepository, times(1)).save(any(Transaction.class));
	}

	@Test
	void transfer_insufficientStock_throws() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("A", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("A").productId(productId).quantity(1).minStock(0).build()));

		assertThrows(InsufficientStockException.class, () -> service.transfer(TransferRequest.builder()
				.productId(productId).sourceStoreId("A").targetStoreId("B").quantity(3).build()));
	}

	@Test
	void registerEntry_createsOrUpdatesAndAddsTransaction() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("S1", productId)).thenReturn(Optional.empty());

		service.registerEntry(MovementRequest.builder().productId(productId).storeId("S1").quantity(5).build());

		verify(inventoryRepository).save(any(Inventory.class));
		verify(transactionRepository).save(any(Transaction.class));
	}

	@Test
	void registerOut_checksStockAndSavesTransaction() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("S1", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("S1").productId(productId).quantity(5).minStock(0).build()));

		service.registerOut(MovementRequest.builder().productId(productId).storeId("S1").quantity(3).build());

		verify(inventoryRepository).save(any(Inventory.class));
		verify(transactionRepository).save(any(Transaction.class));
	}

	@Test
	void registerOut_insufficient_throws() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("S1", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("S1").productId(productId).quantity(2).minStock(0).build()));

		assertThrows(InsufficientStockException.class, () ->
				service.registerOut(MovementRequest.builder().productId(productId).storeId("S1").quantity(3).build()));
	}

	@Test
	void loadInitialStock_blocksWhenAlreadyInitialized() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("S1", productId))
				.thenReturn(Optional.of(Inventory.builder().storeId("S1").productId(productId).quantity(1).minStock(0).build()));

		assertThrows(ConflictException.class, () ->
				service.loadInitialStock(StockLoadRequest.builder().productId(productId).storeId("S1").quantity(10).build()));
	}

	@Test
	void loadInitialStock_initializesAndCreatesInTransaction() {
		UUID productId = UUID.randomUUID();
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
		when(inventoryRepository.findByStoreIdAndProductId("S1", productId)).thenReturn(Optional.empty());

		service.loadInitialStock(StockLoadRequest.builder()
				.productId(productId).storeId("S1").quantity(10).minStock(2).build());

		verify(inventoryRepository).save(any(Inventory.class));
		verify(transactionRepository).save(any(Transaction.class));
	}

	@Test
	void listLowStockAlerts_mapsResults() {
		UUID productId = UUID.randomUUID();
		when(inventoryRepository.findLowStockItems()).thenReturn(List.of(Inventory.builder()
				.productId(productId).storeId("S1").quantity(1).minStock(2).build()));
		when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder()
				.id(productId).name("N").sku("SKU").price(new BigDecimal("9.99")).build()));

		List<LowStockProductResponse> list = service.listLowStockAlerts();
		assertEquals(1, list.size());
		assertEquals("N", list.get(0).getProductName());
	}

	@Test
	void listHistory_returnsMappedPage() {
		UUID productId = UUID.randomUUID();
		Transaction tx = Transaction.builder()
				.id(UUID.randomUUID())
				.productId(productId)
				.sourceStoreId("A")
				.targetStoreId("B")
				.quantity(3)
				.type(Transaction.Type.TRANSFER)
				.build();
		Pageable pageable = PageRequest.of(0, 10);
		when(transactionRepository.findHistory(eq(productId), eq("A"), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(tx), pageable, 1));

		Page<TransactionResponse> page = service.listHistory(productId, "A", pageable);
		assertEquals(1, page.getTotalElements());
		assertEquals("A", page.getContent().get(0).getSourceStoreId());
	}
}


