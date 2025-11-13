package com.demo.eshop.config;

import com.demo.eshop.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
// 매 요청 마다 1번 실행
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        /* 클라이언트가 Header에 Authorization 토큰을 보냈는지 확인*/
        String header = request.getHeader("Authorization");

        /* 토큰이 없거나, Barer 형식이 아니면 -> 걍 통과 (뒤에서 시큐리티가 막을 것임)*/
        if(header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        /* Bearer [토큰 값] 에서 토큰 값 추출*/
        String token = header.substring(7);

        /* JwtUtil로 토큰이 유효한지(위조 또는 만료 안되었는지) 검사*/
        if(jwtUtil.validateToken(token)){

            /* 토큰이 유효 -> 토큰에서 사용자 ID(email) 꺼냄*/
            String email = jwtUtil.getUsernameFromToken(token);

            /* 동사무소에가서 사용자 ID로 표준 신분증(UserDetails) 발급 */
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);

            /* 인증된 사용자 입니다라는 임시 신분증 발급*/
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            /* 스프링 시큐리티에 현재 요청에 이 임시심분증 등록  -> 이제부터 이 사용자 인증된 사용자로 바뀜*/
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }

        /* 다음 필터로 요청 전달 (통과) */
        filterChain.doFilter(request, response);
    }
}
