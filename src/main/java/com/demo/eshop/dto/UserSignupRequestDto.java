package com.demo.eshop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequestDto {
    private String email;
    private String password;
    private String username;

    //role은 관리자가 강제로 주입하는 것이 아님으로 DTO에서 받지 않음.
}
