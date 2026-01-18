package com.demo.eshop.integration;

import com.demo.eshop.domain.*;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Transactional
public class OrderQueryIntegrationTest {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Batch Size 적용 확인: 주문 목록 조회 시 쿼리 개수가 최적화되어야 한다.")
    void nPlusOneCheck(){
        //Given
        User user = new User("nplus1@test.com", "1234", "tester", UserRoleEnum.USER);
        userRepository.save(user);

        Product product = new Product("item", 1000, 100);
        productRepository.save(product);

        // 주문 10개
        for(int i = 0; i< 10; i++){
            OrderItem item = OrderItem.createOrderItem(product, 1);
            Order order = Order.createOrder(user, List.of(item));
            orderRepository.save(order);
        }

        em.flush();
        em.clear(); // 영속성 컨텍스트 초기화( 쿼리 발생 유도)

        System.out.println("================ Start Query COunt");

        //When
        Page<Order> orders = orderRepository.findAllByUser(user, PageRequest.of(0, 10));

        // 실제 OrderItem 접근 시 쿼리가 나가는지 확인  - 배치 사이즈 작동
        for(Order order : orders){
            System.out.println("주문 상품 개수: " + order.getOrderItems().size());
        }

        System.out.println("================= 쿼리 카운트 종료 =================");

        //Then
        assertThat(orders.getContent()).hasSize(10);
        // 콘솔 로그에서 "Hibernate: select ... from order_item ... where order_id in (?, ?, ...)"
        // 쿼리가 '단 한 번' 나갔는지 눈으로 확인하면 합격!
    }
}
