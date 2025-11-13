package com.demo.eshop.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage()); // 부모(런타임이셉션)에 메시지 전달
        this.errorCode = errorCode;
    }
}
