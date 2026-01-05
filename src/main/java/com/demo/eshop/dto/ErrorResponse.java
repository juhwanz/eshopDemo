package com.demo.eshop.dto;

import com.demo.eshop.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final String code;      // 에러 코드
    private final String message;   // 에러 메시지

    // 빌더 패턴 : 객체 생성 시, 생성자 순서 필요 없이 쏙쏙 넣어주는.
    // Static Factory Method -> ErrorCode -> ErrorResponse.
    public static ErrorResponse of(ErrorCode errorCode){
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
    // of 메서드 - 중간 변환기 역할 ErrorCode만 주면, 빌더를 사용해 자동으로 변환 공장.
}
