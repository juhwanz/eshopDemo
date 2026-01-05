package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    // id, user, oderItems, orderDate, status

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문할 회원 (N : 1) | 주문 - User
    @ManyToOne(fetch = FetchType.LAZY)      //주문 정보만 필요하면 주문 정보만. ( 성능 최적화 )
    @JoinColumn(name = "user_id")
    private User user;

    // 주문할 상품들 ( 1 : N ) | cascade - 주문서 삭제 시, 딸린 주문들도 같이 삭제.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;        // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status;             // 주문 상태

    // 양방향 설정.
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 복잡한 주문 생성 한 번에. [캡슐화]
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
