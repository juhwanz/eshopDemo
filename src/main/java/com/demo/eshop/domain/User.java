package com.demo.eshop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users") // Table 이름 명시적 기입.
public class User {
    // id, email, password, username

    @Id     // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) //중복 제거
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)

    @Enumerated(value = EnumType.STRING) //  DB에 "USER", "ADMIN" 문자열로 저장
    private UserRoleEnum role; // 권한 필드 추가


    /* 회원 가입시, 쉽게 만들기 위해 추가.*/
    public User(String email, String password, String username, UserRoleEnum role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }
}