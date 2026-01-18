package com.demo.eshop.service;

import com.demo.eshop.domain.*;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// DB 없이 Mock을 써서 흐름만 빠르게 검증.
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService; // 테스트 대상

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductService productService;  //DDD

    @Test
    @DisplayName(" 주문 성공시, 재고 감소 요청 -> 주문 저장.")
    void orderSuccess(){
        //Given
        Long userId = 1L;
        Long productId = 100L;
        int count = 2;

        User user = new User("user@test.com", "pw", "user", UserRoleEnum.USER);
        Product product = new Product("item", 10000, 10);

        //Mocking : 가짜 행동 정의
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // ProductService가 호출되면 재고가 줄어든 상품을 반환한다고 가정
        given(productService.decreaseStock(productId, count)).willReturn(product);

        //When
        orderService.order(userId, productId, count);

        //Then
        // ProductService의 decreaseStock이 호출되었는지 확인 [캐시 무효화 트리거]
        verify(productService, times(1)).decreaseStock(productId, count);
        // OrderRepo의 save가 호출되었는지 확인
        verify(orderRepository, times(1)).save(any(Order.class));

    }

    @Test
    @DisplayName("주문 실패 : 존재하지 않은 유저 -> 예외 발생")
    void orderFail(){
        //Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Tehn
        assertThatThrownBy(() -> orderService.order(userId, 1L, 1))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
