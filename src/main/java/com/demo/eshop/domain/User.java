package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// User -> Role -> UserDetails : 보안의 기초 구조 [User 엔티티를 UserDetailsImpl로 감싸서 시큐리티 컨텍스트에 태우는 흐름]
// User엔티티가 직접 UserDetails 구현 안함? public class User implements UserDetails처럼 엔티티가 바로 인페 구현 하기도 함.
// -> 편하게 엔티티에 implements하면 되는데, 왜 굳이 UserDetailsImpl라는 래퍼 클래스 따로 생성?
// -> SRP(단일 책임 원칙)을 지키기 위해서. User엔티티는 DB와 매핑되는 데이터 구조 책임, UserDetails는 스프링 시큐리티의 인증 객체로서 동작.
// 스프링 시큐리티 흐름 : 회원가입 -> 로그인 -> 인증(JWT).

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users") // Table 이름 명시적 기입.
public class User {
    // id, email, password, username

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) //중복 제거
    private String email;


    // 비밀 번호 저장 방식 평문? Salt 어디있음? -> UserService나 SecurityConfig에 인코더[BCryptPasswordEncoder]를 사용해 해시화
    // 저 인코드는 내부적으로 Salt 생성해 함께 해싱.
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    // JPA 기본값은 ORDINAL인데, STRING으로 바꾼 이유(문자열이라 공간 더 차지함에도), ORDINAL은 Enum순서 0,1,2 순서로 저장되는데, 중간에
    // GUEST같은 새로운 권한 추가시, 순서가 바뀌면서 DB데이터가 꼬이는 문제.
    @Enumerated(value = EnumType.STRING) //  DB에 "USER", "ADMIN" 문자열로 저장
    private UserRoleEnum role; // 권한 필드 추가

    public User(String email, String password, String username, UserRoleEnum role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }
}

// 유효성 검사 부분이 아쉽
// @Column(unique=true)만 있고, 이메일 형식인지 아닌지 검사하는 로직이 없음. UserSignupRequestDto에서 @Email이나 @Pattern으로 정규식 검사하고 있는지 확인
// 엔티티에는 없어도 DTO에서 막아야 함.
// BaseEntity : 생성일/수정일 부재.
// 보통 회원가입일은 마케팅이나 CS처리할 때 필수.
