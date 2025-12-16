package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
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
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

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
        //ExecutorService : 병렬 작업을 도와주는 자바 일꾼
        ExecutorService exS = Executors.newFixedThreadPool(32);
        // CountDownLatch : 100명의 작업이 다 끝날 때 까지 기다리게 하는 신호등
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 4. 실행
        for(int i = 0; i< threadCount; i++){
            exS.submit( () ->{
                try{
                    orderService.order(userId, productId, 1); // 1개 주문
                }finally {
                    latch.countDown(); // 끝났음을 보고.
                }
            });
        }

        latch.await(); // 100개 다 끝날때 까지 대기

        // 5. [검증] 결과 확인
        Product updatedProduct = productRepository.findById(productId).orElseThrow();

        // 기대값 : 0
        // 실제값 : 90
        System.out.println(" 최종 남은 재고: " + updatedProduct.getStockQuantity());
        assertEquals(0, updatedProduct.getStockQuantity());
    }
}
