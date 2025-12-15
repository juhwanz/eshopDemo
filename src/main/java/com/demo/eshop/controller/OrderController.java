package com.demo.eshop.controller;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.dto.OrderRequestDto;
import com.demo.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문하기 API
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestBody OrderRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails // 토큰에서 사용자 정보 꺼내기
            ){
        // 1. 토큰에 있는 User 정보에서 ID만 꺼내기
        // UserDetailsImpl에 @Getter을 붙여야 getUser() 가능
        Long userId = userDetails.getUser().getId();

        // 2. 서비스에게 주문 요청 (상품 ID, 수량 전달)
        Long orderId = orderService.order(userId, requestDto.getProductId(), requestDto.getCount());

        return ResponseEntity.ok(orderId);
    }
}
