package com.demo.eshop.contoller;

import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductRequestDto;
import com.demo.eshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Product getProductById(@PathVariable Long productId){
        return productService.getProductById(productId);
    }
}
