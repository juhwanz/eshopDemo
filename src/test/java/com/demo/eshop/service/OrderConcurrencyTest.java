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
    private OrderRepository orderRepository;

    @BeforeEach
    void clean() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 동시에_100명이_주문하면_재고가_0이_되어야한다() throws InterruptedException{
        Product product = new Product("한정판 신발", 10000, 100);
        productRepository.save(product);
        Long productId = product.getId();

        User user = new User("teste2@test.com", "1234", "tester", UserRoleEnum.USER);
        userRepository.save(user);
        Long userId = user.getId();

        int threadCount = 100;
        ExecutorService exS = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i< threadCount; i++){
            exS.submit( () ->{
                try{
                    orderService.order(userId, productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Product updatedProduct = productRepository.findById(productId).orElseThrow();

        System.out.println("최종 남은 재고: " + updatedProduct.getStockQuantity());
        assertEquals(0, updatedProduct.getStockQuantity());
    }
}