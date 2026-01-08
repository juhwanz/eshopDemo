package com.demo.eshop.controller;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Long registerProduct(@RequestBody ProductDto.RegisterRequest requestDto){
        return productService.registerProduct(requestDto);
    }

    @GetMapping("/{productId}")
    public ProductDto.Response getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    // [수정됨] 페이징 파라미터(page, size)를 받아서 처리
    @GetMapping("/search")
    public Page<ProductDto.Response> searchProducts(
            ProductDto.SearchCondition condition,
            @PageableDefault(size = 10) Pageable pageable // 기본값: 1페이지당 10개
    ){
        return productService.search(condition, pageable);
    }
}
