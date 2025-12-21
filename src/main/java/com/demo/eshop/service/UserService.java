package com.demo.eshop.service;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.TokenDto;
import com.demo.eshop.dto.UserLoginRequestDto;
import com.demo.eshop.dto.UserSignupRequestDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Config에서 @Bean으로 등록한 녀석을 주입받음
    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;

    /* 회원 가입 */
    public void signup(UserSignupRequestDto requestDto){

        /* 이메일 중복 확인 */
        String email = requestDto.getEmail();
        Optional<User> checkUser = userRepository.findByEmail(email);
        if(checkUser.isPresent()) throw new BusinessException(ErrorCode.EMAIL_DUPLICATION); // 정의된 예외로 교체

        /* 비밀번호 암호화*/
        String password = passwordEncoder.encode(requestDto.getPassword());

        /* 사용자 권한 설저 (기본값 : USER) */
        //Todo UserSignupRequestDto에 adminToken필드를 만들어서, 특정 암호가 맞을때만, 관리자 권한 부여
        //UserRoleEnum role = UserRoleEnum.USER; // 임시로 테스트 주석
        UserRoleEnum role = UserRoleEnum.ADMIN; // 테스트 코드

        /* DTO -> Domain객체로 변환 */
        User user = new User(requestDto.getEmail(), password, requestDto.getUsername(), role);

        /* DB에 저장 */
        userRepository.save(user);
    }

    // refesh Token추가로 수정.
    public TokenDto login(UserLoginRequestDto requestDto){ // String -> TokenDto.
        /* 사용자 확인 (이메일)*/
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 정의된 규칙으로 교체

        /* 비밀번호 확인 */
        //원본 비번과 DB에 저장된 암호화된 비번 비교
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH); // 정의된 규칙으로 교체
        }

        /* Access Token 발급*/
        String accessToken = jwtUtil.createToken(user.getEmail(), user.getRole());

        /* Refresh Token 발급 (Redis 저장용) */
        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        /* redis에 리프레시 토큰 저장 ( Key : "RT:이메일", Value: 토큰, 유효기간 : 14일
         */
        redisTemplate.opsForValue().set(
                "RT:" + user.getEmail(), refreshToken, 14, TimeUnit.DAYS
        );

        /* 두 토큰을 DTO로 담아 반환 */
        return new TokenDto(accessToken, refreshToken);
    }
}
