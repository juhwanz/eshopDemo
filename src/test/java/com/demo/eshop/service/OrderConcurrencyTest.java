package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository; // 청소용

    // 🧹 테스트 실행 전에 DB를 싹 비워주는 청소부
    @BeforeEach
    void clean() {
        // 외래키(Foreign Key) 관계 때문에 자식 데이터(주문상품, 주문)부터 지워야 함!
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 동시에_100명이_주문하면_재고가_0이_되어야한다() throws InterruptedException{
        // 1. [준비] 재고 100개
        Product product = new Product("한정판 신발", 10000, 100);
        productRepository.save(product);
        Long productId = product.getId();

        // 2. [준비] 주문할 유저 생성
        User user = new User("teste2r@test.com", "1234", "tester", UserRoleEnum.USER);
        userRepository.save(user);
        Long userId = user.getId();

        // 3. [동시성 세팅] 100명의 멀티 스레드 준비
        int threadCount = 100;
        ExecutorService exS = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 4. 실행
        for(int i = 0; i< threadCount; i++){
            exS.submit( () ->{
                try{
                    // 100번 모두 같은 유저가 주문한다고 가정 (로직상 허용된다면)
                    // 만약 '1인 1주문' 제한이 있다면 여기서 에러가 날 수 있음.
                    // 포트폴리오용 단순 부하 테스트라면 OK.
                    orderService.order(userId, productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 5. [검증] 결과 확인
        Product updatedProduct = productRepository.findById(productId).orElseThrow();

        System.out.println("최종 남은 재고: " + updatedProduct.getStockQuantity());
        assertEquals(0, updatedProduct.getStockQuantity());
    }
}