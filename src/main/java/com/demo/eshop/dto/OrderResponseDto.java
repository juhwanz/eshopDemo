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

        // 이미 Repo에서 'product'를 가져왔기에 DB 쿼리 안나감. (순수 메모리 조회)
        public OrderItemDto(OrderItem orderItem){
            // orderItem.getProduct()를 호출할 때마다 DB 조회 발생 N+1 문제.
            // 호출 순간 -> DB에 SELECT * FROM product WHERE id = ? 쿼리가 상품 갯수만큼 날라감
            // DTO 변환시 상품이름을 가져오네요? 여기서 성능 문제 발생하지 않으려면 레포 쿼리 어떻게 짜야했음?
            // -> Order와 OrderItem만 페치 조인하면 안되거, OrderItem과 연관된 Product까지 3중으로 패치 조인을 해야 완벽.
            this.productName = orderItem.getProduct().getName();
            this.count = orderItem.getCount();
            this.orderPrice = orderItem.getOrderPrice();
        }
    }
}
