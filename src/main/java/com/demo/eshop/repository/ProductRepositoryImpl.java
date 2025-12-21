package com.demo.eshop.repository;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.demo.eshop.domain.QProduct.product; // ⭐️ Q파일 static import

// QueryDSL사용 -> JPQL이나 Mybatis대신한 이유? 타입 안정성(Type Safety).
// JPQL은 문자열로 작성되어 오타가 있어도 런티아임에러(실행)만 알 수 있음. 이것은 자바 코드로 쿼리 작성 -> 필드명이 틀리면 컴파일 시점에 빨간줄이 그어져 배포 전에 에러 잡을 수 있음.
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 단순히 selectFrom(product) -> 만약 Product엔티티 안에 User나 Review같은 연관 관계(1:N, N:1)가 있다면 -> N+1문제 발생
    //TOdo -> 단일 엔티티 조회라 괜찮지만, 연관 엔티티를 같이 가져와야 한다면, .join()대신 .fetchjoin()을 사용해 해결해야함.
    @Override
    // List 반환하면 -> fetch 하는 순간. OOM발생 가능성 -> 무조건 페이징 해야함.
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
    // BooleanBuilder대신 Expression쓴 이유? 빌더에는 .and(), .or()를 체이닝하는 방식도 많이 존재. 메서드 분리 이유?
    //가독성과 재사용 -> 빌더를 쓰면 if문이 쿼리로 작성 로직 사이에 끼임. -> nameConatains()처럼 조건을 메서드로 불리하면 재사용 가능 + null 반환시 where절에서 자동 무시 -> 동적 쿼리 깔끔.
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