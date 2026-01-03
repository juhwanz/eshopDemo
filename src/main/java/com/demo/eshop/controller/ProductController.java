package com.demo.eshop.controller;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/search")
    public List<ProductDto.Response> searchProducts(ProductDto.SearchCondition condition){

        List<Product> products = productService.search(condition);

        return products.stream()
                .map(ProductDto.Response::new)
                .toList();
    }
}
