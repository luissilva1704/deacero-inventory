package com.deacero.inventario.service;

import com.deacero.inventario.entities.Inventory;
import com.deacero.inventario.entities.Product;
import com.deacero.inventario.entities.Transaction;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.deacero.inventario.exception.ResourceNotFoundException;
import com.deacero.inventario.exception.InsufficientStockException;
import com.deacero.inventario.exception.ConflictException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
            TransactionRepository transactionRepository,
            ProductRepository productRepository,
            InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public List<InventoryItemResponse> getInventoryByStore(String storeId) {
        List<Inventory> list = inventoryRepository.findByStoreId(storeId);
        List<InventoryItemResponse> result = new ArrayList<>();
        for (Inventory inv : list) {
            result.add(inventoryMapper.toItemResponse(inv));
        }
        return result;
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request) {
        UUID productId = request.getProductId();
        // Validate product exists
        productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Inventory source = inventoryRepository.findByStoreIdAndProductId(request.getSourceStoreId(), productId)
                .orElseGet(() -> Inventory.builder()
                        .storeId(request.getSourceStoreId())
                        .productId(productId)
                        .quantity(0)
                        .minStock(0)
                        .build());

        if (source.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock in source store");
        }

        Inventory target = inventoryRepository.findByStoreIdAndProductId(request.getTargetStoreId(), productId)
                .orElseGet(() -> Inventory.builder()
                        .storeId(request.getTargetStoreId())
                        .productId(productId)
                        .quantity(0)
                        .minStock(0)
                        .build());

        source.setQuantity(source.getQuantity() - request.getQuantity());
        target.setQuantity(target.getQuantity() + request.getQuantity());

        inventoryRepository.save(source);
        inventoryRepository.save(target);

        Transaction tx = Transaction.builder()
                .productId(productId)
                .sourceStoreId(request.getSourceStoreId())
                .targetStoreId(request.getTargetStoreId())
                .quantity(request.getQuantity())
                .type(Transaction.Type.TRANSFER)
                .build();
        transactionRepository.save(tx);
    }

    @Override
    public List<LowStockProductResponse> listLowStockAlerts() {
        List<Inventory> lowStock = inventoryRepository.findLowStockItems();
        List<LowStockProductResponse> result = new ArrayList<>();
        for (Inventory i : lowStock) {
            Optional<Product> productOpt = i.getProductId() != null ? productRepository.findById(i.getProductId())
                    : Optional.empty();
            if (productOpt.isEmpty()) {
                continue;
            }
            Product p = productOpt.get();
            result.add(LowStockProductResponse.builder()
                    .productId(p.getId())
                    .productName(p.getName())
                    .sku(p.getSku())
                    .price(p.getPrice())
                    .storeId(i.getStoreId())
                    .quantity(i.getQuantity())
                    .minStock(i.getMinStock())
                    .build());
        }
        return result;
    }

    @Override
    @Transactional
    public void loadInitialStock(StockLoadRequest request) {
        UUID productId = request.getProductId();
        productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Optional<Inventory> existingInventory = inventoryRepository.findByStoreIdAndProductId(request.getStoreId(),
                productId);

        Inventory inv;
        if (existingInventory.isPresent()) {
            Inventory existing = existingInventory.get();
            if (existing.getQuantity() != null && existing.getQuantity() > 0) {
                throw new ConflictException("Inventory already initialized for this store/product");
            }
            inv = existing;
        } else {
            inv = Inventory.builder()
                    .storeId(request.getStoreId())
                    .productId(productId)
                    .quantity(0)
                    .minStock(0)
                    .build();
        }
        inv.setQuantity(request.getQuantity());
        if (request.getMinStock() != null) {
            inv.setMinStock(request.getMinStock());
        }
        inventoryRepository.save(inv);
        Transaction tx = Transaction.builder()
                .productId(productId)
                .targetStoreId(request.getStoreId())
                .quantity(request.getQuantity())
                .type(Transaction.Type.IN)
                .build();
        transactionRepository.save(tx);

    }

    @Override
    @Transactional
    public void registerEntry(MovementRequest request) {
        UUID productId = request.getProductId();
        productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Inventory inv = inventoryRepository.findByStoreIdAndProductId(request.getStoreId(), productId)
                .orElseGet(() -> Inventory.builder()
                        .storeId(request.getStoreId())
                        .productId(productId)
                        .quantity(0)
                        .minStock(0)
                        .build());
        inv.setQuantity(inv.getQuantity() + request.getQuantity());
        inventoryRepository.save(inv);
        Transaction tx = Transaction.builder()
                .productId(productId)
                .targetStoreId(request.getStoreId())
                .quantity(request.getQuantity())
                .type(Transaction.Type.IN)
                .build();
        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void registerOut(MovementRequest request) {
        UUID productId = request.getProductId();
        productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Inventory inv = inventoryRepository.findByStoreIdAndProductId(request.getStoreId(), productId)
                .orElseGet(() -> Inventory.builder()
                        .storeId(request.getStoreId())
                        .productId(productId)
                        .quantity(0)
                        .minStock(0)
                        .build());
        if (inv.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock in store");
        }
        inv.setQuantity(inv.getQuantity() - request.getQuantity());
        inventoryRepository.save(inv);
        Transaction tx = Transaction.builder()
                .productId(productId)
                .sourceStoreId(request.getStoreId())
                .quantity(request.getQuantity())
                .type(Transaction.Type.OUT)
                .build();
        transactionRepository.save(tx);
    }

    @Override
    public Page<TransactionResponse> listHistory(UUID productId, String storeId, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findHistory(productId, storeId, pageable);
        List<TransactionResponse> content = page.getContent().stream().map(t -> TransactionResponse.builder()
                .id(t.getId())
                .productId(t.getProductId())
                .sourceStoreId(t.getSourceStoreId())
                .targetStoreId(t.getTargetStoreId())
                .quantity(t.getQuantity())
                .timestamp(t.getTimestamp())
                .type(t.getType())
                .build()).toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
}
