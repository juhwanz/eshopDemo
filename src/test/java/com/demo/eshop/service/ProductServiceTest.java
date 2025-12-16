package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductRequestDto;
import com.demo.eshop.dto.ProductResponseDto;
import com.demo.eshop.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        //save는 어차피 return값 없음으로, '가짜 행동' 정의(when)이 없어도 됨
        //수정 추가
        Product fakeSavedProduct = new Product(requestDto.getName(), requestDto.getPrice(), requestDto.getStockQuantity());
        when(productRepository.save(any(Product.class)))
                .thenReturn(fakeSavedProduct);
        // When
        productService.registerProduct(requestDto);

        // Then
        //productRepository의 save가 'Product 클래스' 타입의 '아무(any)' 객체를 받아서
        //'정확히 1번(times(1))' 호출되었는지 검증해라.
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void getProductById_success(){
        //Given
        Long productId = 1L;
        // '가짜' DB에 저장되어 있을 '가짜' 상품 객체를 미리 만듦
        Product fakeProduct = new Product("새우깡", 1500, 100);

        // '가짜 행동' 정의:
        // "만약 productRepository.findById(1L)이 호출되면,"
        // "미리 만든 '가짜' 상품(fakeProduct)을 'Optional'로 감싸서 반환(return)해!"
        when(productRepository.findById(productId)).thenReturn(Optional.of(fakeProduct));

        //When
        ProductResponseDto foundProduct = productService.getProductById(productId);

        // ⭐️ Then (검증)
        // 반환된 상품(foundProduct)이 null이 아닌지,
        // 그리고 이름이 우리가 '가정'한 "새우깡"이 맞는지 확인
        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("새우깡");
    }

    @Test
    @DisplayName("상품 ID로 조회 실패 - 상품 없음")
    void getProductById_fail_notFound(){
        //Given
        Long productId = 999L; // 없는 ID라고 가정

        // '가짜 행동' 정의:
        // "만약 productRepository.findById(999L)이 호출되면,"
        // "상품이 없다는 의미로 'Optional.empty()'를 반환해!"
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // ⭐️ When & Then (실행과 검증을 동시에)
        // "productService.getProductById(999L)를 실행할 때,"
        // "반드시 'IllegalArgumentException' 예외가 '발생(throw)'해야 한다!"
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductById(productId);
        });
    }

}

