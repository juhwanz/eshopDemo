package com.demo.eshop.config;

import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter // <- 이게 있어야 userDetails.getUser()를 할 수 있음.
public class UserDetailsImpl implements UserDetails {

    private final User user;

    public UserDetailsImpl(User user){
        this.user = user;
    }

    /* 이 사용자의 권한 반환 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        UserRoleEnum role = user.getRole();
        String authority = role.name();

        // ROLE_ 접두사 = 시큐리티의 '역할'을 나타내는 기본 규칙
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + authority));
    }

    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public String getUsername(){
        return user.getEmail(); // email을 ID(username)으로 사용
    }

    // 계정 만료/잠금 여부 -> 간단히 true로 설정
    @Override
    public boolean isAccountNonExpired(){return true;}
    @Override
    public boolean isAccountNonLocked(){return true;}
    @Override
    public boolean isCredentialsNonExpired(){return true;}
    @Override
    public boolean isEnabled(){return true;}

}
