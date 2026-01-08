package com.demo.eshop.domain_dto;

import com.demo.eshop.domain.*;
import com.demo.eshop.dto.OrderDto;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

//@SpringBootTest : 스프링 부트 전체 띄움 - 오래 걸림
// POJO 테스트 : 순수 자바 코드만 실행 -> JVM위에서 돌아감.
@DisplayName("도메인 & DTO 유닛 테스트")
public class domainUnitTest {
    // Product : 재고 증가, 재고 감소, 재고 부좃기 에외 발생
    // OderItem : 주문 상품 생성 시 가격 설정 및 상품 재고 차감 연동 확인
    // Order : 주문 생성 메서드
    // DTO Mapping : 누락없이 매핑 하는지.

    @Nested // 테스트 케이스 안에 다른 테스트 클래스 만듦. (Junit 5)
    @DisplayName("Product 엔티티")
    class ProductTest {

        @Test
        @DisplayName("생성자 및 초기 상태 확인")
        void createProduct() {
            Product product = new Product("Test Item", 1000, 10);

            assertThat(product.getName()).isEqualTo("Test Item");
            assertThat(product.getPrice()).isEqualTo(1000);
            assertThat(product.getStockQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("addStock")
        void addStock() {
            Product product = new Product("Item", 1000, 10);

            product.addStock(5);

            assertThat(product.getStockQuantity()).isEqualTo(15);
        }

        @Test
        @DisplayName("removeStock")
        void removeStock() {
            Product product = new Product("Item", 1000, 2);

            assertThatThrownBy(() -> product.removeStock(3))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.OUT_OF_STOCK);
        }
    }

    @Nested
    @DisplayName("OrderItem 엔티티 테스트")
    class OrderItemTest {

        @Test
        @DisplayName("createOrderItem: 주문 상품 생성 시 가격 설정 및 재고 차감")
        void createOrderItem() {
            Product product = new Product("NoteBook", 1000000, 10);
            int count = 2;

            OrderItem orderItem = OrderItem.createOrderItem(product, count);

            assertThat(orderItem.getProduct()).isEqualTo(product);
            assertThat(orderItem.getCount()).isEqualTo(count);
            assertThat(orderItem.getOrderPrice()).isEqualTo(1000000);

            // 상품의 재고가 줄었는지 확인
            assertThat(product.getStockQuantity()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("Order 엔티티 테스트")
    class OrderTest{

        @Test
        @DisplayName("createOrder: 주문 생성 메서드")
        void createOrder(){
            User user = new User("user@test.com", "1234", "tester", UserRoleEnum.USER);
            Product product = new Product("TV", 50000, 10);
            OrderItem orderItem = OrderItem.createOrderItem(product,1);

            Order order = Order.createOrder(user, List.of(orderItem));

            assertThat(order.getUser()).isEqualTo(user); // 주문자 설정 확인
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDER); // 초기 상태 확인
            assertThat(order.getOrderDate()).isNotNull(); // 날짜 생성 확인
            assertThat(order.getOrderItems()).hasSize(1); // 주문 상품 리스트 확인
            assertThat(order.getOrderItems().get(0).getOrder()).isEqualTo(order); // 연관관계 편의 메서드 동작(양방향 매핑) 확인
        }
    }

    @Nested
    @DisplayName("DTO 테스트")
    class DtoTest{

        @Test
        @DisplayName("Prodct -> ProductDto.response")
        void product_DtoResponse(){
            Product product = new Product("Mouse", 5000, 100);

            ProductDto.Response response = new ProductDto.Response(product);

            assertThat(response.getName()).isEqualTo(product.getName());
            assertThat(response.getPrice()).isEqualTo(product.getPrice());
            assertThat(response.getStockQuantity()).isEqualTo(product.getStockQuantity());
        }

        @Test
        @DisplayName("Order -> OrderDto.Response 변환 (중첩 리스트 매핑 포함)")
        void orderToDto() {
            // given
            User user = new User("u", "p", "n", UserRoleEnum.USER);
            Product product = new Product("Keycap", 15000, 50);
            OrderItem orderItem = OrderItem.createOrderItem(product, 2);
            Order order = Order.createOrder(user, List.of(orderItem));

            // when
            OrderDto.Response response = new OrderDto.Response(order);

            // then
            assertThat(response.getOrderStatus()).isEqualTo("ORDER");
            assertThat(response.getOrderItems()).hasSize(1);
            assertThat(response.getOrderItems().get(0).getProductName()).isEqualTo("Keycap");
            assertThat(response.getOrderItems().get(0).getCount()).isEqualTo(2);
            assertThat(response.getOrderItems().get(0).getOrderPrice()).isEqualTo(15000);
        }
    }
}
