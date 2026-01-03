package com.demo.eshop.service;

import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName(" 주문 성공시 재고 차감")
    void order_success(){
        User user = new User();
        Product product = new Product("상품", 100, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(product));

        orderService.order(1L, 1L, 2);


        assertThat(product.getStockQuantity()).isEqualTo(8);
        verify(orderRepository).save(any());
    }
}
