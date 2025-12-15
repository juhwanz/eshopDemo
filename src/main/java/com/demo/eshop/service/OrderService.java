package com.demo.eshop.service;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.OrderItem;
import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // 주문 생성
    @Transactional // 데이터를 변경 하믈 readOnly = false (디폴트)
    public Long order(Long userId, Long productId, int count){

        //1. 엔티티 조회 (회원, 상품)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 2. 주문 상품 생성 ( 재고가 줄어 들음)
        OrderItem orderItem = OrderItem.createOrderItem(product, count);

        // 3. 주문 생성
        Order order = Order.createOrder(user, List.of(orderItem));

        // 4. 주문 저장
        // 중요 : orderItems를 따로 저장 안해도, cascade = ALL 때문에 같이 저장됨.
        orderRepository.save(order);

        return order.getId();

    }
}
