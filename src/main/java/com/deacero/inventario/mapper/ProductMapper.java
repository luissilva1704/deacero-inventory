package com.deacero.inventario.mapper;

import com.deacero.inventario.entities.Product;
import com.deacero.inventario.models.ProductRequest;
import com.deacero.inventario.models.ProductResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
	Product toEntity(ProductRequest request);
	ProductResponse toResponse(Product product);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);

	default Page<ProductResponse> toResponsePage(Page<Product> page, Pageable pageable) {
		List<ProductResponse> content = page.getContent().stream().map(this::toResponse).toList();
		return new PageImpl<>(content, pageable, page.getTotalElements());
	}
}


