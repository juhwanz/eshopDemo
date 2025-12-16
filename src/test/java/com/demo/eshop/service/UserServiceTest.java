package com.demo.eshop.service;

import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserSignupRequestDto;
import com.demo.eshop.exception.BusinessException; // 👈 import 추가 필수!
import com.demo.eshop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // Given
        UserSignupRequestDto requestDto = new UserSignupRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        requestDto.setUsername("tester");

        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(requestDto.getPassword()))
                .thenReturn("encoded_pw");

        // When
        userService.signup(requestDto);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_when_email_is_duplicated() {
        // Given
        UserSignupRequestDto requestDto = new UserSignupRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        requestDto.setUsername("tester");

        User fakeUser = new User("test@example.com", "any_pw", "any_name", UserRoleEnum.USER);

        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.of(fakeUser));

        // ⭐️ When & Then
        // IllegalArgumentException -> BusinessException 으로 변경!
        assertThrows(BusinessException.class, () -> {
            userService.signup(requestDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}