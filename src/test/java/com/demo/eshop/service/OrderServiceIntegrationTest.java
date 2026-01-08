package com.demo.eshop.service;

import com.demo.eshop.domain.*;
import com.demo.eshop.dto.OrderDto;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest     // 통합 테스트
@Transactional      // 테스트 종료시 데이터 롤백 (테스트 격리)
public class OrderServiceIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    EntityManager eM;

    @Test
    @DisplayName("배치 사이즈 적용 : N + 1 문제 해결 검증")
    void batchSize(){
        User user = new User("test@email.com", "1234", "tester", UserRoleEnum.USER);
        userRepository.save(user);

        Product product1 = new Product("상품 1", 1000, 100);
        Product product2 = new Product("상품 2", 2000, 100);
        productRepository.saveAll(List.of(product1, product2));

        // 주문 생성
        for(int i = 0; i< 10; i++){
            OrderItem item1 = OrderItem.createOrderItem(product1, 1);
            OrderItem item2 = OrderItem.createOrderItem(product2, 1);
            Order order = Order.createOrder(user, List.of(item1, item2));
            orderRepository.save(order);
        }

        eM.flush();
        eM.clear();

        System.out.println("=============== [ 검증 시작 ] ===================");

        //List<OrderDto.Response> orders = orderService.getOrders(user.getId());

        System.out.println("=============== [ 검증 종료 ] ===================");

       // assertThat(orders).hasSize(10);
    }
}
