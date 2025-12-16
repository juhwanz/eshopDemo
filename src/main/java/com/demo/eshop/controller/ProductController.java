package com.demo.eshop.controller;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductRequestDto;
import com.demo.eshop.dto.ProductResponseDto;
import com.demo.eshop.dto.ProductSearchCondition;
import com.demo.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // json으로 응답
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    //서비스 주입
    private final ProductService productService;

    /*상품 등록 API*/
    @PostMapping
    public Long registerProduct(@RequestBody ProductRequestDto requestDto){
        return productService.registerProduct(requestDto);
    }

    @GetMapping("/{productId}")
    /*상품 단건 조회*/
    public ProductResponseDto getProductById(@PathVariable Long productId) {
        // Service가 이제 DTO를 주니까, 그대로 리턴하면 됩니다.
        return productService.getProductById(productId);
    }

    @GetMapping("/search")
    // 1. 반환 타입을 List<Product> -> List<ProductResponseDto>로 변경
    public List<ProductResponseDto> searchProducts(ProductSearchCondition condition){

        // 2. 서비스에서 받아온 Entity 리스트
        List<Product> products = productService.search(condition);

        // 3. Entity -> DTO로 변환 (포장지로 싸기)
        return products.stream()
                .map(ProductResponseDto::new) // 하나씩 DTO로 변환
                .toList();
    }
}
