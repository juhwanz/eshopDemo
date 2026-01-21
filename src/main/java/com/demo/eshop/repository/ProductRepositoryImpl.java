package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.demo.eshop.domain.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> search(ProductDto.SearchCondition condition, Pageable pageable) {
        // 1. 실제 데이터 조회 (페이징 적용)
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        nameContains(condition.getName()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                )
                // 전형적인 Offset 페이징(Limit-Offset)
                    // 만약 만 개의 상품을 요청 -> DB는 앞의 만 개 다 읽은 다음 버리고, 10개를 가져옴. -> 성능 저하 가능성.
                .offset(pageable.getOffset())   // 페이지 번호 (0부터 시작)
                .limit(pageable.getPageSize())  // 페이지 사이즈 (가져올 개수)
                .fetch();

        // 2. 전체 카운트 쿼리 (Count Query)
        // 조건에 맞는 전체 개수를 알아야 전체 페이지 수를 계산할 수 있음
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        nameContains(condition.getName()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                );

        // 3. Page 객체 생성 및 반환
        // (PageableExecutionUtils는 데이터가 사이즈보다 적으면 카운트 쿼리를 생략하는 등 최적화를 해줍니다)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // No offSet
    @Override
    public Slice<Product> searchNoOffset(Long lastProductId, ProductDto.SearchCondition condition, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(
                        ltProductId(lastProductId), // 핵심: lastProductId보다 작은 ID 조회
                        nameContains(condition.getName()),
                        priceBetween(condition.getMinPrice(), condition.getMaxPrice())
                )
                .orderBy(product.id.desc()) // 최신순 (No-Offset은 순서가 중요함)
                .limit(pageable.getPageSize() + 1) // 다음 페이지 있는지 확인하려고 +1 조회
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize()); // +1개 확인했으니 제거
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    // 동적 쿼리 조건들
    private BooleanExpression ltProductId(Long lastProductId) {
        return lastProductId == null ? null : product.id.lt(lastProductId);
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.contains(name) : null;
    }

    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        if (minPrice == null && maxPrice == null) return null;
        if (minPrice != null && maxPrice != null) return product.price.between(minPrice, maxPrice);
        if (minPrice != null) return product.price.goe(minPrice);
        return product.price.loe(maxPrice);
    }
}