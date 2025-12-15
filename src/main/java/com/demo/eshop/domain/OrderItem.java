package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Entity
@Getter @Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어떤 상품을 샀는지? (N:1관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // 어느 주문서에 속하는지 (N:1관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;


    private int orderPrice; // 주문 당시 가격
    private int count;      // 주문 수량

    // 생성 메서드 (추후 서비스에서 씀)
    public static OrderItem createOrderItem(Product product, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setCount(count);
        orderItem.setOrderPrice(product.getPrice()); // 현재 상품 가격을 주문 가격으로 설정
        // 주문하면 재고를 까야함 (핵심 비즈니스 로직)
        product.removeStock(count);

        return orderItem;
    }



}
