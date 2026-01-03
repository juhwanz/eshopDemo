package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import static com.demo.eshop.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {


    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> search(ProductDto.SearchCondition condition) {
        return queryFactory
                .selectFrom(product)
                .where(
                        nameContains(condition.getName()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                )
                .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.contains(name) : null;
    }

    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }
        if (minPrice != null && maxPrice != null) {
            return product.price.between(minPrice, maxPrice);
        }
        if (minPrice != null) {
            return product.price.goe(minPrice);
        }
        return product.price.loe(maxPrice);
    }
}