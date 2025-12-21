package com.demo.eshop.service;

import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.OrderItem;
import com.demo.eshop.domain.Product;
import com.demo.eshop.domain.User;
import com.demo.eshop.dto.OrderResponseDto;
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

    // 동시성 문제 해결 [낙관 락, Redis 분산 락, Java synchronized]방법은 많음 -> 비관적 락
    // -> synchronized : Scale-out되면 무용지물
    // -> 낙관적 락(@Version) : 충돌이 많이 발생하는 선착순 이벤트 -> 재시도 로직 때문에 오히려DB부하 커짐
    // -> 비관적 락 : 충돌이 잦은 환경에서도 DB레벨에서 확실하게 줄을 세워(직렬화 효과)재고 오류 원천 차단
    /*TOdo 단점 : DeadLock, TimeOut 가능성 -> 전체 처리량 저하 => Tx 짧게 유지하고, 필요 시 Redis를 활용한 분산락 전환.*/
    // -> Redis ???
    // 주문 생성
    @Transactional // 데이터를 변경 하믈 readOnly = false (디폴트)
    public Long order(Long userId, Long productId, int count){

        //1. 엔티티 조회 (회원, 상품) -> 동시성 문제 발생
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        //-> 동시성 문제 발생
        //Product product = productRepository.findById(productId)
                //.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        //락을 걸고 조회 (해결)
        Product product = productRepository.findByIdWithPessimisticLock(productId)
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

    // 주문 목록 조회
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrders(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 유저의 모든 주문 조회 ( 여기선 쿼리 1방) -> 기존의 느린 방식
        // List<Order> orders = orderRepository.findAllByUserOrderByIdDesc(user);

        // 1. 변경 -> fetch join을 사용한 '한방 쿼리'방식
        List<Order> orders = orderRepository.findAllByUserWithFetchJoin(user);

        // 2. 주문(엔티티) -> 주문 응답(DTO)로 변환햇서 반환
        // -> 이 과정에서 각 주문마다 상품 정보를 가져오기 ㅜ이해 쿼리 엄청 나감 (N+1)
        return orders.stream()
                .map(OrderResponseDto::new)
                .toList();
    }
}
