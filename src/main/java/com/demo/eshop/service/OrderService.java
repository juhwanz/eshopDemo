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

    @Transactional
    public Long order(Long userId, Long productId, int count){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdWithPessimisticLock(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        OrderItem orderItem = OrderItem.createOrderItem(product, count);

        Order order = Order.createOrder(user, List.of(orderItem));

        orderRepository.save(order);

        return order.getId();

    }

    @Transactional(readOnly = true)
    public List<OrderDto.Response> getOrders(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Order> orders = orderRepository.findAllByUserWithFetchJoin(user);

        return orders.stream()
                .map(OrderDto.Response::new)
                .toList();
    }
}
