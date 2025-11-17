package com.deacero.inventario.mapper;

import com.deacero.inventario.entities.Inventory;
import com.deacero.inventario.models.InventoryItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
	@Mapping(target = "productId", source = "productId")
	@Mapping(target = "storeId", source = "storeId")
	@Mapping(target = "quantity", source = "quantity")
	@Mapping(target = "minStock", source = "minStock")
	InventoryItemResponse toItemResponse(Inventory inv);
}


