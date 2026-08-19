package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.ProductCreateRequest;
import com.example.gamesphere.dto.request.ProductUpdateRequest;
import com.example.gamesphere.dto.response.ProductResponse;
import com.example.gamesphere.entity.Product;
import com.example.gamesphere.util.DiscountCalculator;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class   ProductMapper {

    @Autowired
    protected DiscountCalculator discountCalculator;

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "active", constant = "true")
    public abstract Product toEntity(ProductCreateRequest request);

    @Mapping(target = "categories", expression = "java(product.getCategories().stream()" +
            ".map(c -> c.getName()).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "finalPrice", expression = "java(calculateFinalPrice(product))")
    @Mapping(target = "discountPercentage", expression = "java(calculateDiscountPercentage(product))")
    @Mapping(target = "gameId", expression = "java(product.getGame() != null ? product.getGame().getId() : null)")
    @Mapping(target = "gameTitle", expression = "java(product.getGame() != null ? product.getGame().getTitle() : null)")
    public abstract ProductResponse toResponse(Product product);

    @Mapping(target = "categories", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(@MappingTarget Product product, ProductUpdateRequest request);

    protected BigDecimal calculateFinalPrice(Product product) {
        return discountCalculator.finalPrice(product.getPrice(), product.getDiscountPrice());
    }

    protected BigDecimal calculateDiscountPercentage(Product product) {
        return discountCalculator.discountPercentage(product.getPrice(), product.getDiscountPrice());
    }
}
