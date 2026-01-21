package com.demo.eshop.integration;

import com.demo.eshop.domain.*;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import com.demo.eshop.service.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
public class OrderQueryIntegrationTest {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    void setup(){
        // 데이터 초기화
        // 데이터 초기화
        User user = userRepository.save(new User("nplus1@test.com", "1234", "tester", UserRoleEnum.USER));
        Product product = productRepository.save(new Product("Test Item", 1000, 100));

        // 주문 10개 생성 (각 주문에 아이템 1개씩)
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            OrderItem item = OrderItem.createOrderItem(product, 1);
            orders.add(Order.createOrder(user, List.of(item)));
        }
        orderRepository.saveAll(orders);

        em.flush();
        em.clear(); // 1차 캐시 비우기 (쿼리 발생 유도)
    }

    @Test
    @DisplayName("N+1 검증: 주문 10개 조회 시 쿼리가 몇 번 나가는가?")
    void checkNPlusOne() {
        System.out.println("========== [조회 시작] ==========");

        // When: 주문 목록 조회 (PageSize = 10)
        orderService.getOrders(
                userRepository.findByEmail("nplus1@test.com").get().getId(),
                PageRequest.of(0, 10)
        );

        System.out.println("========== [조회 종료] ==========");

        // 눈으로 로그 확인:
        // 1. select from orders ... (1번)
        // 2. select from order_item ... (1번? 아니면 10번?)
    }
}
