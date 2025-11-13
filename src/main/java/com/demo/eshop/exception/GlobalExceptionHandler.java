package com.demo.eshop.exception;

import com.demo.eshop.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // AOP 핵심, 모든 RestController의 예외를 여기서 컨트롤
public class GlobalExceptionHandler {

    /* 만약 BusinessException예외가 터지면, 이 메서드가 잡아라*/
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e){

        ErrorCode errorCode = e.getErrorCode();

        /* ErrorCode -> ErrorResponse DTO*/
        ErrorResponse response = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    /* 우리가 잡지 못한 모든 에외*/
    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        // "알 수 없는 서버 오류" 응답을 생성
        ErrorResponse response = ErrorResponse
                .builder()
                .code("SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다: " + e.getMessage())
                .build();

        // "500 Internal Server Error" 상태 코드로 반환
        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
