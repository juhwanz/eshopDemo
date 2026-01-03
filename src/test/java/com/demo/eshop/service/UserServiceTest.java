package com.demo.eshop.service;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserDto;
import com.demo.eshop.exception.BusinessException; // 👈 import 추가 필수!
import com.demo.eshop.repository.UserRepository;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock private RedisTemplate<String, String> redisTemplate;

    @Mock private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 성공 시나리오")
    void signup_success() {

        UserDto.SignupRequest request= new UserDto.SignupRequest();
        request.setEmail("test@test.com");
        request.setPassword("1234");
        request.setUsername("tester");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded_pw");

        userService.signup(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_duplicate() {
        UserDto.SignupRequest request = new UserDto.SignupRequest();
        request.setEmail("duplicate@test.com");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User())); // 이미 존재

        assertThrows(BusinessException.class, () -> userService.signup(request));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        UserDto.LoginRequest request = new UserDto.LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("1234");

        User fakeUser = new User("test@test.com", "encodedPw", "tester", null);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(fakeUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.createToken(any(), any())).thenReturn("access");
        when(jwtUtil.createRefreshToken(any())).thenReturn("refresh");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        UserDto.TokenResponse response = userService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
    }
}