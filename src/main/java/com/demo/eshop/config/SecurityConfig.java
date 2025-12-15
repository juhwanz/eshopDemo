package com.demo.eshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // 스프링 시큐리티를 활성화 합니다.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /* 스프링이 이 생성자를 호출해 JwtAuthenticationFilter' Bean을 DI */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean //이 메서드가 반환하는 객체(BCryptPasswordEncoder)를 Spring 컨테이너에 등록해주세요!"
    public PasswordEncoder passwordEncoder() {
        // BCrypt는 '해시' 방식의 암호화 도구입니다. (가장 많이 쓰임)
        return new BCryptPasswordEncoder();
    }

    /* 보안 규칙 정의 메서드 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                /* CSRF 보호 비활성화 : Stateless JWT 방식은 세션/큐키를 사용 X -> CSRF 공격에 안전*/
                .csrf(csrf->csrf.disable())
                /* 세샨(Session) 관리 정책 설정 : 상태를 저장하지 않는 stateless 기 때문에*/
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* 보안 규칙 (인가 설정)*/
                .authorizeHttpRequests(authz ->authz
                        /* 이 API들은 모두 접근 허용 */
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        //Swagger 관련 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        /* 상품 조회 API 모두 접근 허용*/
                        .requestMatchers(HttpMethod.GET,"/api/products/**").permitAll()
                        /* 상품등록 API ADMIN 권한만 접근 허용*/
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                        // 나머지 인증(로그인)만 하면 접근 허용
                        .anyRequest().authenticated()
                )
                /* 검표원(jwtAuthenticationFilter)을 시큐리티의 기본 로그인 필터(UsernamePasswordAuthenticationFilter)보다 먼저 배치해라*/
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
       return http.build();
    }
}
