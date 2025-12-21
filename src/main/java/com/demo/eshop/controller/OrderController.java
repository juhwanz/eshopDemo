package com.demo.eshop.controller;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.dto.OrderRequestDto;
import com.demo.eshop.dto.OrderResponseDto;
import com.demo.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문하기 API
    @PostMapping
    // AuthenticationPrincipal만 붙였는데 어떻게 유저 정보가 들어오나요? JwtAuthenticationFilter에서 인증 성공 시 생성한
    // Authentication 객체를 SecurityContextHolder에 저장했기 때문,
    // 스프링 시큐리티가 이 컨텍스트에서 Principal(우리가 만든 UserDetailsImpl)을 꺼내서 컨트롤러 파라미터에 DI
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

    // 내 주문 내역 조회 API
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = userDetails.getUser().getId();
        List<OrderResponseDto> orders = orderService.getOrders(userId);
        return ResponseEntity.ok(orders);
    }
}
