package com.demo.eshop.service;

import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserSignupRequestDto;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Config에서 @Bean으로 등록한 녀석을 주입받음

    /* 회원 가입 */
    public void signup(UserSignupRequestDto requestDto){

        /* 이메일 중복 확인 */
        String email = requestDto.getEmail();
        Optional<User> checkUser = userRepository.findByEmail(email);
        if(checkUser.isPresent()) throw new IllegalArgumentException("이미 가입된 이메일 입니다.");

        /* 비밀번호 암호화*/
        String password = passwordEncoder.encode(requestDto.getPassword());

        /* 사용자 권한 설저 (기본값 : USER) */
        UserRoleEnum role = UserRoleEnum.USER;

        /* DTO -> Domain객체로 변환 */
        User user = new User(requestDto.getEmail(), password, requestDto.getUsername(), role);

        /* DB에 저장 */
        userRepository.save(user);
    }
}
