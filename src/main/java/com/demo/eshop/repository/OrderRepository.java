package com.demo.eshop.repository;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserOrderByIdDesc(User user);

    @Query("select distinct o from Order o " +
            "join fetch o.orderItems oi " +
            "join fetch oi.product p " +
            "where o.user = :user " +
            "order by o.id desc")
    List<Order> findAllByUserWithFetchJoin(@Param("user") User user);
}
