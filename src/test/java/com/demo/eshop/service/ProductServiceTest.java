package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductRequestDto;
import com.demo.eshop.dto.ProductResponseDto;
import com.demo.eshop.exception.BusinessException; // 👈 import 추가 필수!
import com.demo.eshop.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // 👈 ID 주입용 유틸

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 등록 성공")
    void registerProduct_success(){
        // Given
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setName("새우깡");
        requestDto.setPrice(1500);
        requestDto.setStockQuantity(100);

        Product fakeSavedProduct = new Product(requestDto.getName(), requestDto.getPrice(), requestDto.getStockQuantity());
        // 💡 가짜 객체(Mock)라서 ID가 null이면 서비스 로직이 꼬일 수 있음. 강제로 ID 1L 부여.
        ReflectionTestUtils.setField(fakeSavedProduct, "id", 1L);

        when(productRepository.save(any(Product.class)))
                .thenReturn(fakeSavedProduct);

        // When
        Long savedId = productService.registerProduct(requestDto);

        // Then
        assertThat(savedId).isEqualTo(1L); // ID가 잘 반환되었는지 확인
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void getProductById_success(){
        // Given
        Long productId = 1L;
        Product fakeProduct = new Product("새우깡", 1500, 100);
        ReflectionTestUtils.setField(fakeProduct, "id", 1L); // ID 주입

        when(productRepository.findById(productId)).thenReturn(Optional.of(fakeProduct));

        // When
        ProductResponseDto foundProduct = productService.getProductById(productId);

        // Then
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("새우깡");
        assertThat(foundProduct.getPrice()).isEqualTo(1500);
    }

    @Test
    @DisplayName("상품 ID로 조회 실패 - 상품 없음")
    void getProductById_fail_notFound(){
        // Given
        Long productId = 999L;

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ⭐️ When & Then
        // IllegalArgumentException -> BusinessException으로 변경!
        assertThrows(BusinessException.class, () -> {
            productService.getProductById(productId);
        });
    }
}