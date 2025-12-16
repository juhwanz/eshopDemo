package com.demo.eshop.dto;

import lombok.Data;

@Data
public class ProductSearchCondition {
    private String name;        //상품 명 포함 검색
    private Integer minPrice;   // 최소 가격
    private Integer maxPrice;   // 최대 가격
}
