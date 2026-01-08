package com.demo.eshop.serviceLayer;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.UserRepository;
import com.demo.eshop.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 성공: 비밀번호가 암호화되어 저장된다")
    void signup_success() {
        // given
        UserDto.SignupRequest request = new UserDto.SignupRequest();
        request.setEmail("new@test.com");
        request.setPassword("plainPassword");
        request.setUsername("newUser");

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode("plainPassword")).willReturn("encodedPassword");

        // when
        userService.signup(request);

        // then
        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("new@test.com") &&
                        user.getPassword().equals("encodedPassword") && // 암호화 확인
                        user.getRole() == UserRoleEnum.ADMIN // 코드상 ADMIN 고정 확인
        ));
    }

    @Test
    @DisplayName("로그인 성공: Access/Refresh 토큰 발급 및 Redis 저장")
    void login_success() {
        // given
        UserDto.LoginRequest request = new UserDto.LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        User user = new User("user@test.com", "encodedPw", "user", UserRoleEnum.USER);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(anyString(), any())).willReturn("access_token");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refresh_token");

        // Redis Mocking
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        UserDto.TokenResponse response = userService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");

        // Redis에 Refresh Token 저장 확인 (키, 값, 만료시간)
        verify(valueOperations).set(eq("RT:user@test.com"), eq("refresh_token"), eq(14L), eq(TimeUnit.DAYS));
    }
}