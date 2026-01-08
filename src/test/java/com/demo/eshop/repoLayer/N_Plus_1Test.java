package com.demo.eshop.repoLayer;

import com.demo.eshop.config.QueryDslConfig;
import com.demo.eshop.domain.*;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
public class N_Plus_1Test {

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    EntityManager em;


    User user;

    @BeforeEach
    void setUp(){
        user = new User("tester@test.com", "1234", "tester", UserRoleEnum.USER);
        userRepository.save(user);

        Product product = new Product("testItem", 1000, 10000);
        productRepository.save(product);

        List<Order> orders = new ArrayList<>();
        for(int i = 0; i<100; i++){
            OrderItem item1 = OrderItem.createOrderItem(product, 1);
            OrderItem item2 = OrderItem.createOrderItem(product, 1);
            orders.add(Order.createOrder(user, List.of(item1, item2)));
        }
        orderRepository.saveAll(orders);

        em.flush();
        em.clear();
    }


    @Test
    @DisplayName("성능 비교 batch vs fetch-join")
    void verse(){
        // 1. 일반 조회 (Lazy Loading + Batch Size)
        StopWatch stopWatch = new StopWatch("Batch");
        stopWatch.start("1. findAll (Lazy + Batch)");

        List<Order> orders1 = orderRepository.findAllByUserOrderByIdDesc(user);

        // 지연 로딩 강제 초기화 (N + 1 발생 지점 확인)
        for(Order order : orders1){
            order.getOrderItems().size(); // 추가 쿼리 발생 가능
            order.getOrderItems().get(0).getProduct().getName(); // 상품 명 접근
        }

        stopWatch.stop();
        System.out.println("=====완료 . 개수 : " + orders1.size());
        em.clear();

        // 2. fethjoin
        stopWatch.start("2. fetch-join");

        List<Order> orders2 = orderRepository.findAllByUserWithFetchJoin(user);

        // 초기화( 이미 로딩 되었으므로 쿼리 안나감)
        for(Order order : orders2){
            order.getOrderItems().size(); // 추가 쿼리 발생 가능
            order.getOrderItems().get(0).getProduct().getName(); // 상품 명 접근
        }

        stopWatch.stop();
        System.out.println("=======완료. 개수 :" + orders2.size());

        em.clear();
        System.out.println(stopWatch.prettyPrint());

        assertThat(orders1).hasSize(100);
        assertThat(orders2).hasSize(100);
        // 속도는 fetch join이 더 빠르다.
            // 배치사이즈는 : 퀄가 2번(오더 조회 1번, 아이템 조회 1번)나가면서 DB 네트워크 왕복 비용 발생
            // 패치 조인 : SQL 한방으로 모든 데이터 가져옴.
    }

    @Test
    @DisplayName("아키텍쳐 검증 : 페이징 시 배치가 정답")
    void pagingArchitectureTest(){
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Order> batchPage = orderRepository.findAllByUser(user, pageRequest);

        System.out.println(" batch paging 개수 : " + batchPage.getContent().size());

        // 지연 로딩 발생 시 Batch 작동 확인
        batchPage.getContent().forEach(o -> o.getOrderItems().size());

        assertThat(batchPage.getContent()).hasSize(10);

        // 2. [Bad] Fetch Join 전략 (메모리 페이징 위험)
        // 경고 로그 확인용: HHH000104 ... applying in memory!
        Page<Order> fetchPage = orderRepository.findAllByUserWithFetchJoinAndPaging(user, pageRequest);

        System.out.println(">>> Fetch Paging 개수: " + fetchPage.getContent().size()); // 10개 (하지만 메모리에서 자른 것)

        assertThat(fetchPage.getContent()).hasSize(10);

    }
}
