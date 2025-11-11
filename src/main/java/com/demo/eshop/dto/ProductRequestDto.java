package com.demo.eshop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter // Controller가 JSON을 객체로 만들 때 Setter도 필요할 수 있어요.
public class ProductRequestDto {

    // client로 부터 받을 데이터만 = name, price, stockQuantity
    private String name;
    private int price;
    private int stockQuantity;

}
