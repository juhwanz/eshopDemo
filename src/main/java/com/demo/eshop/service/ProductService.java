package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductRequestDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /* 이 생성자 자동으로 생성
    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }*/

    /*상품 등록*/
    //DTO -> domain으로 변환
    @Transactional
    public Long registerProduct(ProductRequestDto requestDto){
        // 개발자용 생성자 사용
        Product product = new Product(
                requestDto.getName(),
                requestDto.getPrice(),
                requestDto.getStockQuantity()
        );

        // 창고에 저장하라고 시킴
        // ???
        Product savedProduct = productRepository.save(product);

        return savedProduct.getId();
    }

    /*상품 단건 조회*/
    @Transactional(readOnly = true) // '읽기' 작업(성능 최적화)
    public Product getProductById(Long productId) {
        // '창고'에서 ID로 상품을 찾습니다.
        // 만약 없으면(Optional), "상품이 없다"고 에러를 발생시킵니다.
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND)); // 정의된 규칙으로 교체
    }

}
