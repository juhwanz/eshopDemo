package com.demo.eshop.dto;

import com.demo.eshop.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // 빌더 패턴 : 객체 생성 깔끔하게 해줌
public class ErrorResponse {

    private final String code;
    private final String message;

    /* ErrorCode(객체) -> ErrorResponse DTO로 변환 정적 메서드*/
    public static ErrorResponse of(ErrorCode errorCode){
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}
