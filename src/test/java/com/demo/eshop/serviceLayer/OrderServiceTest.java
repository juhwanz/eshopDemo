package com.demo.eshop.serviceLayer;

// 서비스 계층 핵심 - Mocking(가짜 객체)를 사용해 외부 의존성을 격리하고, 오직 순수 비즈니스 로직의 흐름과 예외 처리 검증
// Order Service : 주문시 비관적 락 메서드가 호출되는지 검증하여 동시성 제어 의도 확인

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.OrderRepository;
import com.demo.eshop.repository.ProductRepository;
import com.demo.eshop.repository.UserRepository;
import com.demo.eshop.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @Test
    @DisplayName("주문 성공: 비관적 락을 걸고 주문을 생성한다")
    void order_success() {
        // given
        Long userId = 1L;
        Long productId = 100L;
        int count = 5;

        User user = new User("test@email.com", "pw", "tester", UserRoleEnum.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Product product = new Product("Test Item", 1000, 10);
        ReflectionTestUtils.setField(product, "id", productId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 핵심: 일반 findById가 아닌 Lock 메서드 호출 모킹
        given(productRepository.findByIdWithPessimisticLock(productId)).willReturn(Optional.of(product));

        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", 999L);
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        Long orderId = orderService.order(userId, productId, count);

        // then
        assertThat(orderId).isEqualTo(999L);
        assertThat(product.getStockQuantity()).isEqualTo(5); // 10 - 5 = 5 (재고 감소 확인)

        // Lock 메서드 호출 여부 검증
        verify(productRepository).findByIdWithPessimisticLock(productId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 실패: 재고가 부족하면 예외가 발생한다")
    void order_fail_outOfStock() {
        // given
        User user = new User();
        Product product = new Product("Item", 1000, 2); // 재고 2개

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(productRepository.findByIdWithPessimisticLock(1L)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> orderService.order(1L, 1L, 5)) // 5개 주문 시도
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_STOCK);
    }
}
