package com.demo.eshop.service;

import com.demo.eshop.config.UserDetailsImpl;
import com.demo.eshop.domain.User;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 스프링 시큐리티의 UserDetailsService 인터페이스르 구현

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        //email을 username으로 쓰기로
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        //DB에서 찾은 User 객체 -> '표준 신분증(UserDetailsImpl)로 전환
        return new UserDetailsImpl(user);
    }
}
