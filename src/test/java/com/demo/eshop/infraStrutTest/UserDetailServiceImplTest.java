package com.demo.eshop.infraStrutTest;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.UserRepository;
import com.demo.eshop.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserDetailServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 로드 성공")
    void loadUserByUsername_success() {
        // given
        String email = "test@email.com";
        User user = new User(email, "password", "username", UserRoleEnum.USER);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }
    @Test
    @DisplayName("사용자를 찾을 수 없으면 예외 발생")
    void loadUserByUsername_notFound() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@email.com"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
