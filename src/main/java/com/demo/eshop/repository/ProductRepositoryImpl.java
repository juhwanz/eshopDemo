package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.demo.eshop.domain.QProduct.product; // ⭐️ Q파일 static import

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> search(ProductSearchCondition condition) {
        return queryFactory
                .selectFrom(product)
                .where(
                        nameContains(condition.getName()),      // 이름 조건
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice()) // 가격 조건
                )
                .fetch();
    }

    // ⭐️ 동적 쿼리의 핵심! (null이면 조건 무시 = 전체 검색)
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
            return product.price.goe(minPrice); // Greater Or Equal (>=)
        }
        return product.price.loe(maxPrice); // Less Or Equal (<=)
    }
}