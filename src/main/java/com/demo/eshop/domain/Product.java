package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 자동 생성 (JPA가 테이블 생성시 빈 껍데기 만들 떄 사용)
public class Product {
    // id, name, price, stockQuantity
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

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

    // [비즈니스 로직] 재고 증가 ( 주문 취소 시)
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    // [비즈니스 로직] 재고 감소 (주문 시)
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock<0){
            throw new IllegalArgumentException("재고가 부족합니다");
            // 추후 BusinessException(ErrorCode.OUT_OF_STOCK)으로 바꾸면 더 완벽.
        }
        this.stockQuantity = restStock;
    }
}
