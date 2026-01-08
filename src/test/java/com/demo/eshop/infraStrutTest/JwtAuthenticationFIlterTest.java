package com.demo.eshop.infraStrutTest;


import com.demo.eshop.config.JwtAuthenticationFilter;
import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFIlterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp(){
        SecurityContextHolder.clearContext(); // 테스트 간 컨텍스트 오염 방지
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("정상적인 토큰이 헤더에 있을 때 인증 객체가 SecurityContext에 저장된다")
    void doFilterInternal_validToken() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid_access_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        String username = "testUser";
        User user = new User(username, "pw", "name", UserRoleEnum.USER);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Mocking behavior
        given(jwtUtil.validateToken("valid_access_token")).willReturn(true);
        given(jwtUtil.getUsernameFromToken("valid_access_token")).willReturn(username);
        given(userDetailsService.loadUserByUsername(username)).willReturn(userDetails);

        // when
        //jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 없이 필터 체인을 통과한다")
    void doFilterInternal_noHeader() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest(); // 헤더 없음
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        //jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull(); // 인증 객체 없음
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 경우 인증 객체가 저장되지 않는다")
    void doFilterInternal_invalidToken() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid_token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtUtil.validateToken("invalid_token")).willReturn(false);

        // when
        //jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        verify(filterChain).doFilter(request, response);
    }

}
