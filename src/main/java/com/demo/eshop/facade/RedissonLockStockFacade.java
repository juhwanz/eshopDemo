package com.demo.eshop.facade;

import com.demo.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;
    private final ProductService productService;

    public void decreaseStock(Long productId, int quantity) {
        // 락 이름 (고유)
        RLock lock = redissonClient.getLock("product:stock: " + productId);

        try {
            // 1. 락 흭득 시도 (최대 10초 대기, 락 흭득 후 1초 지나면 자동 해제)
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                log.error("락 흭득 실패");
                return;
            }

            // 2. 실제 비즈니스 로직 실행 (기존 메서드)
            // 주의: ProductService의 decreaseStock에서 @Transactional은 유지하되
            // findByIdWithPessimisticLock 대신 일반 findById를 사용하는 메서드를 따로 만들어야 함
            productService.decreaseStockWithoutLock(productId, quantity);
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            // 3. 락 해제
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }
}
