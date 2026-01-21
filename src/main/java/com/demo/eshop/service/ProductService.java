package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    // [핵심] 테스트 설정 파일에서 주입받습니다. 없으면 0 (지연 없음)
    @Value("${test.simulation.delay-ms:0}")
    private int simulationDelay;

    @Transactional
    public Long registerProduct(ProductDto.RegisterRequest requestDto){
        Product product = new Product(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getStockQuantity()
        );

        Product savedProduct = productRepository.save(product);

        return savedProduct.getId();
    }

    @Cacheable(value = "products", key = "#productId", cacheManager = "cacheManager")
    @Transactional(readOnly = true)
    public ProductDto.Response getProductById(Long productId) {
        Product product =  productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return new ProductDto.Response(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto.Response> search(ProductDto.SearchCondition condition, Pageable pageable){
        // Repository에서 Page<Product>를 받아와서 map으로 변환
        return productRepository.search(condition, pageable)
                .map(ProductDto.Response::new);
    }

    /*
        DB 비관적 락 사용 - 성능 비교 테스트의 'Before' 데이터 측적용.
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public Product decreaseStock(Long productId, int quantity) {
        // 1. 상품을 비관적 락(Lock)을 걸고 가져온다.
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        simulateDelay(); // 지연 시뮬레이션

        // 2. 도메인 메서드 호출 (재고 감소)
        product.removeStock(quantity);

        // 3. 변경된 상품 객체를 반환 (주문 생성에 써야 하니까)
        return product;
    }

    /*
        Reids 분삭 락(Facade) 내부에서 Tx 범위로 감싸서 호출함. -> DB 락을 걸지 않음
     */

    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public Product decreaseStockWithoutLock(Long productId, int quantity){
        // 1. 일반 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        simulateDelay(); // 여기도 공평하게 지연 시뮬레이션

        product.removeStock(quantity);

        return product;
    }

    // 테스트일 때만 동작하는 메서드
    private void simulateDelay() {
        if (simulationDelay > 0) {
            try { Thread.sleep(simulationDelay); } catch (InterruptedException e) {}
        }
    }

    // [추가] 상품 가격 수정 (핵심: 수정 시 캐시 삭제 -> 정합성 유지)
    @Transactional
    @CacheEvict(value = "products", key = "#productId") // 👈 이게 핵심! (수정 시 Redis 키 삭제)
    public ProductDto.Response updateProductPrice(Long productId, int newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.updatePrice(newPrice); // 도메인 메서드 호출 (Dirty Checking으로 DB 업데이트)

        // 리턴값은 중요하지 않음. 메서드가 정상 종료되면 캐시가 날아감.
        return new ProductDto.Response(product);
    }
}
