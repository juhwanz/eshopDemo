package com.demo.eshop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDto {
    // email, password
    private String email;
    private String password; //암호화 되지 않은 원본 비밀번호???
}
