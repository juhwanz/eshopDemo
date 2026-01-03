package com.demo.eshop.controller;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.dto.OrderDto;
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

    @PostMapping
    public ResponseEntity<Long> createOrder(
            @RequestBody OrderDto.Request requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ){
        Long userId = userDetails.getUser().getId();

        Long orderId = orderService.order(userId, requestDto.getProductId(), requestDto.getCount());

        return ResponseEntity.ok(orderId);
    }


    @GetMapping
    public ResponseEntity<List<OrderDto.Response>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Long userId = userDetails.getUser().getId();
        List<OrderDto.Response> orders = orderService.getOrders(userId);
        return ResponseEntity.ok(orders);
    }
}
