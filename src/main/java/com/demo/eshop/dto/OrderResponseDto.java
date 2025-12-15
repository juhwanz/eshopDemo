package com.demo.eshop.dto;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {

    private Long orderId;
    private String orderStatus; // 주문 상태
    private LocalDateTime orderDate;
    private List<OrderItemDto> orderItems; // 주문 상태 리스트

    // 생성자 : 엔티티 -> DTO 변환
    public OrderResponseDto(Order order){
        this.orderId = order.getId();
        this.orderStatus = order.getStatus().name();
        this.orderDate = order.getOrderDate();
        // 주문 상품 리스트도 DTO로 변환해서 넣기
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }

    // 내부 클래스로 상품 정보 DTO 정의 ( 주문 안에 들어갈 상품 정보)
    @Getter
    public static class OrderItemDto{
        private String productName; // 상품명 - 여기서 N+1 문제 발생 예정
        private int count;
        private int orderPrice;

        public OrderItemDto(OrderItem orderItem){
            // orderItem.getProduct()를 호출할 때마다 DB 조회 발생
            this.productName = orderItem.getProduct().getName();
            this.count = orderItem.getCount();
            this.orderPrice = orderItem.getOrderPrice();
        }
    }
}
