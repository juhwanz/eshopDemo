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
/* 보안 핵심 로직*/
// 매 요청 마다 1번 실행
// 왜 이 필터를 기본 로그인 필터(UsernamePasswordAu..)보다 먼저 실행?
// -> 스프링 시큐리티는 기본적으로 세션 기반 인증이나, 우리는 세션 안씀.기본 필터 전에 JWT 유효성 검사를 선행해서 유효하면 인증처리(SecurityContextHolder에 저장)를 미리 끝내놓기 위함.
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
            //JWT에 정보가 다 있는데, 왜 굳이 DB에서 또 조회(loadUser..)? -> 성능 저하지만, 보안상 안전 <- 토큰 발급 이후에 사용자가 탈퇴나 권한 변경
            // DB를 한번 더 확인하면 그런 변경 사항을 실시간 반영( Todo 성능 중시를 위해 Redis 캐싱 도입하는 방법 고려)
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
