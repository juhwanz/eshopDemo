package com.demo.eshop.service;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserLoginRequestDto;
import com.demo.eshop.dto.UserSignupRequestDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Config에서 @Bean으로 등록한 녀석을 주입받음
    private final JwtUtil jwtUtil;

    /* 회원 가입 */
    public void signup(UserSignupRequestDto requestDto){

        /* 이메일 중복 확인 */
        String email = requestDto.getEmail();
        Optional<User> checkUser = userRepository.findByEmail(email);
        if(checkUser.isPresent()) throw new BusinessException(ErrorCode.EMAIL_DUPLICATION); // 정의된 예외로 교체

        /* 비밀번호 암호화*/
        String password = passwordEncoder.encode(requestDto.getPassword());

        /* 사용자 권한 설저 (기본값 : USER) */
        UserRoleEnum role = UserRoleEnum.USER;

        /* DTO -> Domain객체로 변환 */
        User user = new User(requestDto.getEmail(), password, requestDto.getUsername(), role);

        /* DB에 저장 */
        userRepository.save(user);
    }

    public String login(UserLoginRequestDto requestDto){
        /* 사용자 확인 (이메일)*/
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 정의된 규칙으로 교체

        /* 비밀번호 확인 */
        //원본 비번과 DB에 저장된 암호화된 비번 비교
        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH); // 정의된 규칙으로 교체
        }

        /* 비밀번호 일치시 Jwt 발권 */
        return jwtUtil.createToken(user.getEmail(), user.getRole()); // 권한 포함 발급.
    }
}
