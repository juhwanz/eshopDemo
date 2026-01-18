package com.demo.eshop.controller;

import com.demo.eshop.dto.UserDto;
import com.demo.eshop.service.UserService;
import jakarta.validation.Valid;
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

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid UserDto.SignupRequest requestDto){
        userService.signup(requestDto);
        return ResponseEntity.status(201).body("회원가입 성공");
    }

    @PostMapping("/login")
    // 헤더에 넣는 코드 싹 지우고, 그냥 body에 던져줌 -> 프론트에서 쉽게 씀.
    //REST API 표준: "토큰도 클라이언트가 받아야 할 '리소스'의 일부이므로, JSON 응답 본문(Body)에 명시적으로 담아주는 것이 파싱하기 쉽고 직관적이라 판단했습니다."
    //확장성: "추후에 token_type('Bearer'), expires_in(만료시간) 같은 메타 데이터를 같이 내려주기에도 JSON 구조가 훨씬 유리합니다."
    public ResponseEntity<UserDto.TokenResponse> login(@RequestBody @Valid UserDto.LoginRequest requestDto){
        UserDto.TokenResponse tokenDto = userService.login(requestDto);

        return ResponseEntity.ok(tokenDto);
    }
}
