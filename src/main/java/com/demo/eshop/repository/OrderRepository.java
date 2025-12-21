package com.demo.eshop.repository;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 특정 유저의 주문 내역을 최신순(ID 내림차순)으로 가져오기 => N+1 문제 터짐
    List<Order> findAllByUserOrderByIdDesc(User user);

    // 신규 : Fetch Join을 사용한 '성능 최적화'메서드
    // Order를 가져올 때 (select o), OrderItem도 같이 (join fetch oi), Product도 같이(join fetch p) 가져와라!
    @Query("select distinct o from Order o " +
            "join fetch o.orderItems oi " + // 1차 조인 : 주문 + 주문상품
            "join fetch oi.product p " +    // 2차 조인 : 주문상품 + 상품 (N+1문제 해결)
            "where o.user = :user " +
            "order by o.id desc")
    List<Order> findAllByUserWithFetchJoin(@Param("user") User user);
}
