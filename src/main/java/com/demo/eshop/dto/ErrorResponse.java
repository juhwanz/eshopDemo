package com.demo.eshop.dto;

import com.demo.eshop.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

    private final String code;
    private final String message;

    public static ErrorResponse of(ErrorCode errorCode){
        return ErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}
