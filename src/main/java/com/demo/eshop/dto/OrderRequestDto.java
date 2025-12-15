package com.demo.eshop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter @Setter
@NoArgsConstructor
public class OrderRequestDto {
    private Long productId;
    private int count;
}
