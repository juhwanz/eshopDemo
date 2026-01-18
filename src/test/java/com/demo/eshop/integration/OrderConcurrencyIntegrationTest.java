package com.demo.eshop.integration;

import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import com.demo.eshop.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

// [핵심] 외부 파일(yml) 설정을 믿지 않고, 여기서 직접 주입합니다.
// MODE=MySQL을 제거하고 순수 H2 모드로 동작시켜 락 타임아웃 충돌을 방지합니다.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.hikari.maximum-pool-size=50",
        "spring.datasource.hikari.connection-timeout=5000"
})
public class OrderConcurrencyIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시성 제어: 30명이 동시에 주문해도 재고는 정확해야 한다.")
    void concurrencyOrderTest() throws InterruptedException {
        // Given
        int stockQuantity = 30;
        int threadCount = 30; // 30명 동시 접속

        Product product = productRepository.save(new Product("Limited Item", 10000, stockQuantity));
        User user = userRepository.save(new User("tester@test.com", "1234", "tester", UserRoleEnum.USER));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.order(user.getId(), product.getId(), 1);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    System.out.println("주문 실패 로그: " + e.getMessage());
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();

        // 로그 출력 (디버깅)
        System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
        System.out.println("남은 재고: " + updatedProduct.getStockQuantity());

        // 검증: 실패가 0이어야 하고, 재고도 0이어야 함
        assertEquals(0, failCount.get(), "실패한 주문이 없어야 합니다. (락 대기 시간 초과 등)");
        assertEquals(0, updatedProduct.getStockQuantity(), "재고가 0이어야 합니다.");
    }
}