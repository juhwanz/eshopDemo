/*package com.demo.eshop.controller;

import com.demo.eshop.config.SecurityConfig;
import com.demo.eshop.dto.UserDto;
import com.demo.eshop.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        }
)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;

    @Test
    @DisplayName("회원가입 성공: 201 상태코드 반환")
    @WithMockUser // CSRF 토큰 생성을 위해 필요 (보안이 켜져있으므로)
    void signup_success() throws Exception {
        // given
        UserDto.SignupRequest request = new UserDto.SignupRequest();
        request.setEmail("test@email.com");
        request.setPassword("password");
        request.setUsername("tester");

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .with(csrf()) // POST 요청 시 CSRF 토큰 필수
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("회원가입 성공"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 성공: 헤더에 토큰이 포함되어야 한다")
    @WithMockUser
    void login_success() throws Exception {
        // given
        UserDto.LoginRequest request = new UserDto.LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("password");

        UserDto.TokenResponse tokenResponse = new UserDto.TokenResponse("access-token", "refresh-token");
        given(userService.login(any())).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer access-token"))
                .andExpect(header().string("Refresh-Token", "Bearer refresh-token"))
                .andDo(print());
    }
}

package com.demo.eshop.controller;

import com.demo.eshop.config.SecurityConfig;
import com.demo.eshop.domain.Product;
import com.demo.eshop.dto.ProductDto;
import com.demo.eshop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductService productService;

    @Test
    @DisplayName("상품 등록 API")
    @WithMockUser
    void registerProduct() throws Exception {
        // given
        ProductDto.RegisterRequest request = new ProductDto.RegisterRequest();
        request.setName("New Item");
        request.setPrice(1000);
        request.setStockQuantity(100);

        given(productService.registerProduct(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1L))
                .andDo(print());
    }

    @Test
    @DisplayName("상품 검색 API")
    @WithMockUser
    void searchProducts() throws Exception {
        // given
        Product product = new Product("MacBook", 2000000, 10);
        ProductDto.Response response = new ProductDto.Response(product); // DTO 생성

        given(productService.search(any())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/products/search")
                        .param("name", "MacBook")
                        .param("minPrice", "1000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MacBook"))
                .andDo(print());
    }
}

package com.demo.eshop.controller;

import com.demo.eshop.config.SecurityConfig;
import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.domain.Order;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.OrderDto;
import com.demo.eshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;

    private UserDetailsImpl testUserDetails;

    @BeforeEach
    void setUp() {
        // 커스텀 UserDetails 생성 (User 객체 포함)
        User user = new User("user@test.com", "pw", "user", UserRoleEnum.USER);
        ReflectionTestUtils.setField(user, "id", 1L); // ID 강제 주입
        testUserDetails = new UserDetailsImpl(user);
    }

    @Test
    @DisplayName("주문 생성 API: UserDetailsImpl 주입 확인")
    void createOrder() throws Exception {
        // given
        OrderDto.Request request = new OrderDto.Request();
        request.setProductId(100L);
        request.setCount(2);

        given(orderService.order(eq(1L), eq(100L), eq(2))).willReturn(500L);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        // [핵심] 커스텀 Principal 주입
                        .with(user(testUserDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(500L))
                .andDo(print());
    }

    @Test
    @DisplayName("주문 목록 조회 API")
    void getOrders() throws Exception {
        // given
        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", 10L);
        ReflectionTestUtils.setField(order, "orderDate", LocalDateTime.now());
        ReflectionTestUtils.setField(order, "orderItems", new ArrayList<>()); // 빈 리스트
        // OrderStatus 등 필요한 필드는 기본값이 있거나 null 처리됨

        OrderDto.Response response = new OrderDto.Response(order);

        given(orderService.getOrders(eq(1L))).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/orders")
                        .with(user(testUserDetails))) // 인증 객체 주입
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(10L))
                .andDo(print());
    }
}*/