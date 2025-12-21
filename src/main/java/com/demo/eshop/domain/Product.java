package com.demo.eshop.domain;

import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

//DDD(Domain-Driven Design, 도메인 주도 설계) : 도메인 로직을 엔티티안에 응집시킨다.

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 자동 생성 (JPA가 테이블 생성시 빈 껍데기 만들 떄 사용)
// 현재 public으로 열려 있으나, JPA 스펙상 기본 생성자는 protected여도 충분.(퍼블릭일 시 Product product = new Product();로 의미 없는 빈 객체 남발 가능성)
//@NoArgsConstructor(access = AccessLevel.PROTECTED)로 해도 됨.
public class Product {
    // id, name, price, stockQuantity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stockQuantity;

    // 생성자 ( 새로운 상품 만들시 사용 )
    public Product(String name, int price, int stockQuantity){
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    /* 보통 Service계층에서 짜는데, 왜 엔티티 안에 넣었나? 데이터를 가지고 있는 주체가 그 데이터를 관리하는 로직도
     함꼐 가져가야 응집도 상승(객체지향의 원리)*/

    // [비즈니스 로직] 재고 증가 ( 주문 취소 시)
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    // [비즈니스 로직] 재고 감소 (주문 시)

    /**
     동시성 이슈 발생 : -> JavaLevel의 synchronized는 서버가 여러 대일 때 소용 없음 -> DB 비관적 락, 낙관적 락, Redis 분산락을 사용한
     분산 락 고려. OrderConcurrencyTest에서 그 부분 검증
     */
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock<0){
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        this.stockQuantity = restStock;
    }
}
