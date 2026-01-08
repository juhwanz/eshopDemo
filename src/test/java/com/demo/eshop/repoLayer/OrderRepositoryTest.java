package com.demo.eshop.repoLayer;


import com.demo.eshop.config.QueryDslConfig;
import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
public class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("특정 유저의 주문 목록을 ID 내림차순(최신순)으로 조회")
    void findAllByUserOrderByIdDesc(){
        User user1 = new User("user1@test.com", "1234", "user1", UserRoleEnum.USER);
        User user2 = new User("user2@test.com", "1234", "user2", UserRoleEnum.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        //user 1의 주문 3개
        Order order1 = Order.createOrder(user1, List.of());
        Order order2 = Order.createOrder(user1, List.of());
        Order order3 = Order.createOrder(user1, List.of());

        //user 2의 주문 1개
        Order order4 = Order.createOrder(user2, List.of());

        orderRepository.saveAll(List.of(order1, order2, order3, order4));

        //------------------
        List<Order> result = orderRepository.findAllByUserOrderByIdDesc(user1);

        assertThat(result).hasSize(3);

        // user1 외 다른 것 뽑히면 안됨.
        assertThat(result).extracting("user").containsOnly(user1);

        // ID 내림 차순 정렬 확인.
        assertThat(result.get(0).getId()).isEqualTo(order3.getId());
        assertThat(result.get(1).getId()).isEqualTo(order2.getId());
        assertThat(result.get(2).getId()).isEqualTo(order1.getId());

    }

}
