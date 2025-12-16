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
    public List<Product> searchProducts(ProductSearchCondition condition){
        return productService.search(condition);
    }
}
