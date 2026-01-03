package com.demo.eshop.service;

import com.demo.eshop.config.JwtUtil;
import com.demo.eshop.domain.User;
import com.demo.eshop.domain.UserRoleEnum;
import com.demo.eshop.dto.UserDto;
import com.demo.eshop.exception.BusinessException;
import com.demo.eshop.exception.ErrorCode;
import com.demo.eshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;

    public void signup(UserDto.SignupRequest requestDto){

        String email = requestDto.getEmail();

        Optional<User> checkUser = userRepository.findByEmail(email);

        if(checkUser.isPresent()) throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);

        String password = passwordEncoder.encode(requestDto.getPassword());

        UserRoleEnum role = UserRoleEnum.ADMIN;

        User user = new User(requestDto.getEmail(), password, requestDto.getUsername(), role);

        userRepository.save(user);
    }

    public UserDto.TokenResponse login(UserDto.LoginRequest requestDto){
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtUtil.createToken(user.getEmail(), user.getRole());

        String refreshToken = jwtUtil.createRefreshToken(user.getEmail());

        redisTemplate.opsForValue().set(
                "RT:" + user.getEmail(), refreshToken, 14, TimeUnit.DAYS
        );

        return new UserDto.TokenResponse(accessToken, refreshToken);
    }
}
