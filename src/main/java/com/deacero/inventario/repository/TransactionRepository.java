package com.deacero.inventario.repository;

import com.deacero.inventario.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
	@Query("""
					select t from Transaction t
					where (:productId is null or t.productId = :productId)
					and (:storeId is null or t.sourceStoreId = :storeId or t.targetStoreId = :storeId)
					order by t.timestamp desc
			""")
	Page<Transaction> findHistory(
			@Param("productId") UUID productId,
			@Param("storeId") String storeId,
			Pageable pageable);
}
