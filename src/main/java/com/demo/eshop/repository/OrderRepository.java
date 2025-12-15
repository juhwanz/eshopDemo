package com.demo.eshop.repository;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 특정 유저의 주문 내역을 최신순(ID 내림차순)으로 가져오기
    List<Order> findAllByUserOrderByIdDesc(User user);
}
