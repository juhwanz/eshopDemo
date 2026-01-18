package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor// access = AccessLevel.PROTECTED 테스트 때문에 풀어 둠
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화.
@Table(name = "orders")
public class Order {
    // id, user, oerItems, orderDate, status

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

    @CreatedDate // 저장 시 시간 자동 삽입
    @Column(updatable = false)
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
        order.user = user;
        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
        }
        order.status = OrderStatus.ORDER;
        // date는 자동으로 삽입
        return order;
    }

    // 비즈니스 로직 (상태 변경 토로 )
    // 단순히 setStatus(CANCEL) 하는 게 아니라, "주문 취소"라는 행위를 정의
    public void cancel(){
        // 배송 중이거나 완료된 경우 취소 불가 검증 로직 포함
        if(this.status == OrderStatus.COMPLETED){
            throw new IllegalStateException("이미 완료된 주문은 취소가 불가능합니다.");
        }

        this.status = OrderStatus.CANCEL;

        // (선택 사항) 취소 시 재고 원복 로직이 필요하다면 여기에 추가
        // for(OrderItem orderItem : orderItems) {
        //     orderItem.cancel(); // OrderItem에 재고 증가 메서드 필요
        // }
    }
}
