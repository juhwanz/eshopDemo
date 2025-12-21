package com.demo.eshop.controller;

import com.demo.eshop.dto.TokenDto;
import com.demo.eshop.dto.UserLoginRequestDto;
import com.demo.eshop.dto.UserSignupRequestDto;
import com.demo.eshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /* 회원 가입 api*/
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto){
        userService.signup(requestDto);
        // 문자열 대신, '201 Created'같은 명확한 HTTP 상태 반환
        return ResponseEntity.status(201).body("회원가입 성공");
    }

    /* 로그인 API */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequestDto requestDto){
        // Service에서 토큰 2개가 담긴 DTO 받아옴
        TokenDto tokenDto = userService.login(requestDto);

        // 토큰을 body가 아닌, 'Header'에 담아주는 것이 표준
        return ResponseEntity.ok() // 200ok
                .header("Authorization", "Bearer "+tokenDto.getAccessToken()) //Authorization: Bearer [토큰값]
                .header("Refresh-Token", "Bearer "+tokenDto.getRefreshToken()) // RefreshToken 추가.
                .body("로그인 성공");
    }
}
