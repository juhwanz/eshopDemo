package com.demo.eshop.controller;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.dto.OrderDto;
import com.demo.eshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<OrderDto.Response>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)Pageable pageable
            ){
        Long userId = userDetails.getUser().getId();
        Page<OrderDto.Response> orders = orderService.getOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }
}
