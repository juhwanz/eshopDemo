package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public Product decreaseStock(Long productId, int quantity) {
        // 1. 상품을 비관적 락(Lock)을 걸고 가져온다.
        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 도메인 메서드 호출 (재고 감소)
        product.removeStock(quantity);

        // 3. 변경된 상품 객체를 반환 (주문 생성에 써야 하니까)
        return product;
    }
}
