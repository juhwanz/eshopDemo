package com.demo.eshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean //이 메서드가 반환하는 객체(BCryptPasswordEncoder)를 Spring 컨테이너에 등록해주세요!"
    public PasswordEncoder passwordEncoder() {
        // BCrypt는 '해시' 방식의 암호화 도구입니다. (가장 많이 쓰임)
        return new BCryptPasswordEncoder();
    }
}
