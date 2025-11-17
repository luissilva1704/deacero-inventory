package com.deacero.inventario.repository;

import com.deacero.inventario.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByStoreId(String storeId);

    Optional<Inventory> findByStoreIdAndProductId(String storeId, UUID productId);

    @Query("""
        select i.productId
        from Inventory i
        group by i.productId
        having sum(i.quantity) >= :minQuantity
    """)
    List<UUID> findProductIdsWithTotalQuantityAtLeast(@Param("minQuantity") int minQuantity);

    @Query("""
        select i
        from Inventory i
        where i.quantity <= i.minStock
    """)
    List<Inventory> findLowStockItems();
}


