package com.demo.eshop.service;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.OrderItem;
import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.dto.OrderDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional
    public Long order(Long userId, Long productId, int count){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productService.decreaseStock(productId, count);

        OrderItem orderItem = OrderItem.createOrderItem(product, count);

        Order order = Order.createOrder(user, List.of(orderItem));

        orderRepository.save(order);

        return order.getId();

    }

    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrders(Long userId, Pageable pageable){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Fetch join 대신 Batch Size를 활용한 조회로 변경.
        // 1. 주문 목록 조회 (쿼리 1번) -> Order 엔티티 10개 조회 = 10 + 1 = 11
        Page<Order> orderPage = orderRepository.findAllByUser(user, pageable);

        // 2. DTO 변환 과정에서 각 Order마다 gerOrderItems() 호출 ( 지연 로딩 발생)
        return orderPage.map(OrderDto.Response::new);
    }
}
