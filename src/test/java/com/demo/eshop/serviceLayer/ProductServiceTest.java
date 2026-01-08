package com.demo.eshop.serviceLayer;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    ProductService productService;

    @Mock
    ProductRepository productRepository;

    @Test
    @DisplayName("상품 등록 성공")
    void registerProduct() {
        // given
        ProductDto.RegisterRequest request = new ProductDto.RegisterRequest();
        request.setName("New Product");
        request.setPrice(5000);
        request.setStockQuantity(100);

        Product savedProduct = new Product("New Product", 5000, 100);
        ReflectionTestUtils.setField(savedProduct, "id", 1L);

        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        Long resultId = productService.registerProduct(request);

        // then
        assertThat(resultId).isEqualTo(1L);
    }

    @Test
    @DisplayName("상품 단건 조회")
    void getProductById() {
        // given
        Long productId = 1L;
        Product product = new Product("Existing Product", 3000, 50);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductDto.Response response = productService.getProductById(productId);

        // then
        assertThat(response.getName()).isEqualTo("Existing Product");
        assertThat(response.getPrice()).isEqualTo(3000);
    }
}