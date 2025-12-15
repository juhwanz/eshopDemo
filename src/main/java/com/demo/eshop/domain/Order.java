package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // SQL 예약어 ORDER와 겹침 방지
@Setter @Getter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문할 회원 (N : 1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 주문 상품들 ( 1: N)
    // cascade = All : 주문서를 저장/삭제 시, 딸린 주문들도 같이 저장/삭제
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();


    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 (주문, 삭제)

    // 연관관계(양뱡향 세팅) <- 부재 시, user.getOrders()를 했는데, 비어있는 참사
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 생성 메서드(복잡한 주문 생성을 한 번에)
    public static Order createOrder(User user, List<OrderItem> orderItems){
        Order order = new Order();
        order.setUser(user);
        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
}
