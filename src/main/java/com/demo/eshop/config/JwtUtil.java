package com.demo.eshop.config;

import com.demo.eshop.domain.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component // 1. 1일차 개념: Spring 컨테이너에 'Bean'으로 등록
public class JwtUtil {

    private final long expirationTime; // 2. yml에서 주입받을 '만료 시간'
    private Key key; // 3. HMAC-SHA 알고리즘으로 암호화할 '키' 객체

    // 4. 생성자 주입
    public JwtUtil(@Value("${jwt.expiration-time}") long expirationTime) {
        this.expirationTime = expirationTime;
    }

    // 5. @PostConstruct: 'Bean(JwtUtil)'이 생성되고 '의존성 주입'이 완료된 직후에 실행됨
    @PostConstruct
    public void init() {
        // 6. yml의 secret 값을 Base64로 '디코딩'해서 byte 배열로 변환
        String cleanSecretKey = "V293LFRoaXMtaXMtYS1yZWFsbHktbG9uZy1zZWNyZXQta2V5LWZvci1qd3Qtc2VjdXJpdHkhISEK"; // (개선점: @Value("${jwt.secret}")로 읽어와야 함)
        byte[] keyBytes = Base64.getDecoder().decode(cleanSecretKey);

        // 7. 디코딩된 byte 배열을 HMAC-SHA 알고리즘에 맞는 'Key' 객체로 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 8. 토큰 생성 (매표소 직원이 호출)
    public String createToken(String username, UserRoleEnum role) {
        Claims claims = Jwts.claims().setSubject(username); // 'sub' 클레임
        claims.put("auth", role.name()); // "auth"라는 이름으로 ADMIN or USER 문자열을 저장

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setClaims(claims) // ⭐️ Subject 대신 CLaims 객체 통째로 넣기
                .setIssuedAt(now)     // 'Payload': 토큰 발급 시간
                .setExpiration(expiryDate) // 'Payload': 토큰 만료 시간
                .signWith(key)        // ⭐️ 'Signature' 부분: 위조 방지 '서명' (우리의 '비밀 키' 사용)
                .compact();
    }


}