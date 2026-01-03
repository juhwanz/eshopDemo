package com.demo.eshop.controller;

import com.demo.eshop.dto.UserDto;
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

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto.SignupRequest requestDto){
        userService.signup(requestDto);
        return ResponseEntity.status(201).body("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDto.LoginRequest requestDto){
        UserDto.TokenResponse tokenDto = userService.login(requestDto);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer "+tokenDto.getAccessToken())
                .header("Refresh-Token", "Bearer "+tokenDto.getRefreshToken())
                .body("로그인 성공");
    }
}
