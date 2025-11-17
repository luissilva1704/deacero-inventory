package com.deacero.inventario.controller;

import com.deacero.inventario.models.InventoryItemResponse;
import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import com.deacero.inventario.models.LowStockProductResponse;
import com.deacero.inventario.models.MovementRequest;
import com.deacero.inventario.models.StockLoadRequest;
import com.deacero.inventario.models.TransactionResponse;
import com.deacero.inventario.models.TransferRequest;
import com.deacero.inventario.models.GenericResponse;
import com.deacero.inventario.exception.ResourceNotFoundException;
import com.deacero.inventario.service.InventoryService;
import com.deacero.inventario.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.UUID;
import io.swagger.v3.oas.annotations.tags.Tag;
@RestController
@RequestMapping("/deacero/api/v1")
@Tag(name = "Inventory", description = "Inventory management")
public class InventoryController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public InventoryController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    // 1. Gestión de Productos
    @GetMapping("/products")
    @Operation(summary = "List all products")
    @ApiResponse(responseCode = "200", description = "Products fetched")
    public GenericResponse<Page<ProductResponse>> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer stock,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductResponse> data = productService.listProducts(category, minPrice, maxPrice, stock, pageable);
        return GenericResponse.ok(data, "Products fetched", "/deacero/api/v1/products");
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get a product by ID")
    @ApiResponse(responseCode = "200", description = "Product fetched")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public GenericResponse<ProductResponse> getProduct(@PathVariable java.util.UUID id) {
        ProductResponse data = productService.getProduct(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return GenericResponse.ok(data, "Product fetched", "/deacero/api/v1/products/" + id);
    }

    @PostMapping("/products")
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest product) {
        ProductResponse created = productService.createProduct(product);
        return GenericResponse.ok(created, "Product created", "/deacero/api/v1/products");
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update product by Id")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
	public GenericResponse<ProductResponse> updateProduct(@PathVariable java.util.UUID id, @RequestBody ProductRequest product) {
        ProductResponse data = productService.updateProduct(id, product)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return GenericResponse.ok(data, "Product updated", "/deacero/api/v1/products/" + id);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product by Id")
    @ApiResponse(responseCode = "200", description = "Product deleted")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public GenericResponse<Void> deleteProduct(@PathVariable java.util.UUID id) {
        productService.deleteProduct(id);
        return GenericResponse.ok(null, "Product deleted", "/deacero/api/v1/products/" + id);
    }

    // 2. Gestión de Stock
    @GetMapping("/stores/{id}/inventory")
    @Operation(summary = "List inventory by store Id")
    @ApiResponse(responseCode = "200", description = "Inventory fetched")
    @ApiResponse(responseCode = "404", description = "Store not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public GenericResponse<List<InventoryItemResponse>> listInventoryByStore(@PathVariable("id") String storeId) {
        List<InventoryItemResponse> data = inventoryService.getInventoryByStore(storeId);
        return GenericResponse.ok(data, "Inventory fetched", "/deacero/api/v1/stores/" + storeId + "/inventory");
    }

    @PostMapping("/inventory/transfer")
    @Operation(summary = "Transfer inventory between stores")
    @ApiResponse(responseCode = "200", description = "Transfer completed")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public GenericResponse<Void> transferInventory(@RequestBody TransferRequest request) {
        inventoryService.transfer(request);
        return GenericResponse.ok(null, "Transfer completed", "/deacero/api/v1/inventory/transfer");
    }

    @GetMapping("/inventory/alerts")
    @Operation(summary = "List low stock alerts")
    @ApiResponse(responseCode = "200", description = "Low stock alerts fetched")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public GenericResponse<List<LowStockProductResponse>> lowStockAlerts() {
        List<LowStockProductResponse> data = inventoryService.listLowStockAlerts();
        return GenericResponse.ok(data, "Low stock alerts", "/deacero/api/v1/inventory/alerts");
    }

    // 3. Carga inicial, entradas, salidas e historial
    @PostMapping("/inventory/load")
    @Operation(summary = "Load initial stock")
    @ApiResponse(responseCode = "201", description = "Initial stock loaded")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<Void> loadInitial(@Valid @RequestBody StockLoadRequest request) {
        inventoryService.loadInitialStock(request);
        return GenericResponse.ok(null, "Initial stock loaded", "/deacero/api/v1/inventory/load");
    }

    @PostMapping("/inventory/in")
    @Operation(summary = "Register entry into store")
    @ApiResponse(responseCode = "201", description = "Entry registered")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<Void> registerEntry(@Valid @RequestBody MovementRequest request) {
        inventoryService.registerEntry(request);
        return GenericResponse.ok(null, "Entry registered", "/deacero/api/v1/inventory/in");
    }

    @PostMapping("/inventory/out")
    @Operation(summary = "Register out from store")
    @ApiResponse(responseCode = "201", description = "Out registered")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<Void> registerOut(@Valid @RequestBody MovementRequest request) {
        inventoryService.registerOut(request);
        return GenericResponse.ok(null, "Out registered", "/deacero/api/v1/inventory/out");
    }

    @GetMapping("/inventory/history")
    @Operation(summary = "List history of transactions")
    @ApiResponse(responseCode = "200", description = "History of transactions fetched")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public GenericResponse<Page<TransactionResponse>> history(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String storeId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<TransactionResponse> data = inventoryService.listHistory(productId, storeId, pageable);
        return GenericResponse.ok(data, "History fetched", "/deacero/api/v1/inventory/history");
    }

}
