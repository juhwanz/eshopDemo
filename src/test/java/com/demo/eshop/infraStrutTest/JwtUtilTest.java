package com.demo.eshop.infraStrutTest;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.UserRoleEnum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

// JwtUtilTest(유닛 테스트)
public class JwtUtilTest {
    // Jwt 로직 : 토큰 생성, 파싱, 만료 검증, 서명 위조 감지.
    // Filter 동작 : 헤더 파싱, 시큐리티콘텍스트홀더 인증 객체 주입 여부
    // UserDetailsService : DB 조회 및 UserDetails 변환 정확성.

    private JwtUtil jwtUtil;

    //테스트용 32바이트 이상 secret key
    private static final String TEST_SECRET = "testSecretKeyForJwtUnitTestingWhichIsVeryLongAndSecure12345";
    private static final String ENCODED_SECRET = Base64.getEncoder().encodeToString(TEST_SECRET.getBytes());
    private static final long TEST_EXPIRATION = 1000L * 60; // 1분

    @BeforeEach
    void setUp(){
        jwtUtil = new JwtUtil(TEST_EXPIRATION, ENCODED_SECRET);
        jwtUtil.init();
    }

    @Test
    @DisplayName("createToken : 토큰 생성 및 사용자 이름 추출 성공")
    void createToken_and_getUserName(){
        String username = "user@test.com";
        UserRoleEnum userRoleEnum = UserRoleEnum.USER;

        String token = jwtUtil.createToken(username, userRoleEnum);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertThat(token).isNotNull();
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공 (validateToken)")
    void validateToken(){
        String token = jwtUtil.createToken("validUser", UserRoleEnum.USER);

        boolean isValid = jwtUtil.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_fail_expired() throws InterruptedException{
        // 유효시간 짧은 토큰
        JwtUtil shortJwtUtil = new JwtUtil(1L, ENCODED_SECRET);
        shortJwtUtil.init();
        String expiredToken = shortJwtUtil.createToken("expiredUser", UserRoleEnum.USER);

        Thread.sleep(100);
        boolean isValid = shortJwtUtil.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("서명이 위조된 토큰 검증 실패")
    void validateToken_fail_invalidSig(){
        String fakeKeystr = "fakeSecretKeyForJwtUnitTestingWhichIsVeryLongAndSecureFakeee";
        Key fakeKey = Keys.hmacShaKeyFor(fakeKeystr.getBytes(StandardCharsets.UTF_8));

        String fakeToken = Jwts.builder()
                .setSubject("hacker")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(fakeKey) // 다른 키로 서명
                .compact();

        boolean isValid = jwtUtil.validateToken(fakeToken);

        assertThat(isValid).isFalse();
    }
}
