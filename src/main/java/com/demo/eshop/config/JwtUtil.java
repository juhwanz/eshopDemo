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

    private final long expirationTime; // 2. yml에서 주입받을 '만료 시간', AccessToken 만료시간(30분)
    private final String secretKeyString; // 임시로 키 값을 담아둘 변수.
    private Key key; // 3. HMAC-SHA 알고리즘으로 암호화할 '키' 객체

    // [추가] Refresh Token 만료 시간 (2주 = 14일) 하드 코딩
    private final long REFRESH_TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L;

    // 4. 생성자 주입
    // 2. 생성자 수정: expirationTime과 secret을 둘 다 받아오도록 변경
    public JwtUtil(
            @Value("${jwt.expiration-time}") long expirationTime,
            @Value("${jwt.secret}") String secretKeyString // application.yml에서 읽어옴
    ) {
        this.expirationTime = expirationTime;
        this.secretKeyString = secretKeyString;
    }

    // 5. @PostConstruct: 'Bean(JwtUtil)'이 생성되고 '의존성 주입'이 완료된 직후에 실행됨
    @PostConstruct
    public void init() {
        // 3. 하드코딩 된 문자열 삭제! -> 주입받은 변수(secretKeyString) 사용
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 8. 토큰 생성 (매표소 직원이 호출) - access 토큰 생성 뿐(유효시간 짧음, 권한 정보 포함) -> RefreshToken추가해야함
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
    /* [추가] 리프레시 토큰 생성 (유효시간 김, 권한 정보 없음) -> 권한 정보 없는 이유? Access Token 재발급 용도로만 쓰기 때문에.
     */
    public String createRefreshToken(String username){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_TIME);

        return Jwts.builder()
                .setSubject(username) // 누구 것인지는 알아야 함을
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key) // 서명은 키 값으로
                .compact();
    }

    /* 토큰에서 사용자 이름 추출 */
    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key) // 비밀 키로 서명 검증 준비
                .build()
                .parseClaimsJws(token) // 토큰 해독 (parse)
                .getBody()// Payload 부분 가져옴
                .getSubject(); // Subject(email)반환.
    }

    /* 토큰 유효성 검증*/
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (Exception e){
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }


}