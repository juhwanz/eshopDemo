package com.demo.eshop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //HTTP 상태 코드 포함시킴
    //400 BAD_REQUEST : 잘못된 요청 ( 이메일 중복, 비번 틀림 등)
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    //401 UNAUTHORIZED : 인증 실패 ( 토큰 없음, 유효하지 않은 토큰 등)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    // 404 NOT_FOUND : 리소스 찾을 수 없음
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),

    // etc(추가)
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다");
    private final HttpStatus httpStatus; // 400,404 같은 HTTP 상태
    private final String message;       // 비밀번호 오류와 같은 메시지
}
