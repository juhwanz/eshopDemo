package com.demo.eshop.dto;

import com.demo.eshop.domain.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Redis가 데이터를 읽을 시 기본 생성자가 꼭 필요함.
public class ProductResponseDto {

    private Long id;
    private String name;
    private int price;
    private int stockQuantity;

    // Entity -> DTO 변환 생성자
    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
    }
}
