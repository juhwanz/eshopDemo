package com.demo.eshop.service;

import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserSignupRequestDto;
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
    private PasswordEncoder passwordEncoder; // ⭐️ '가짜' 암호화 도구

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // ⭐️ Given (준비)
        UserSignupRequestDto requestDto = new UserSignupRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        requestDto.setUsername("tester");

        // '가짜 행동' 1: "이메일 중복 검사 시, 무조건 '없음(empty)'을 반환해!"
        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.empty());

        // '가짜 행동' 2: "암호화 도구가 'password123'을 받으면, 'encoded_pw'를 반환해!"
        when(passwordEncoder.encode(requestDto.getPassword()))
                .thenReturn("encoded_pw");

        // ⭐️ When (실행)
        userService.signup(requestDto);

        // ⭐️ Then (검증)
        // 'save'가 '정확히 1번' 호출되었는지 검증
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_when_email_is_duplicated() {
        // ⭐️ Given (준비)
        UserSignupRequestDto requestDto = new UserSignupRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("password123");
        requestDto.setUsername("tester");

        // '가짜' DB에 이미 존재하는 '가짜' 유저를 만듦
        User fakeUser = new User("test@example.com", "any_pw", "any_name", UserRoleEnum.USER);

        // '가짜 행동' 1: "이메일 중복 검사 시, '가짜 유저(fakeUser)'를 반환해!"
        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.of(fakeUser));

        // (참고: passwordEncoder.encode는 호출조차 안 될 것이므로 정의할 필요 없음)

        // ⭐️ When & Then (실행과 검증을 동시에)
        // "userService.signup()을 실행할 때,"
        // "반드시 'IllegalArgumentException' 예외가 '발생'해야 한다!"
        assertThrows(IllegalArgumentException.class, () -> {
            userService.signup(requestDto);
        });

        // ⭐️ 추가 검증 (Then)
        // "예외가 터졌으니, 'save'는 '절대(never)' 호출되면 안 된다!"
        verify(userRepository, never()).save(any(User.class));
    }
}