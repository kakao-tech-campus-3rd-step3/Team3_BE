package com.shootdoori.match.exception.handler;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException businessException
    ) {
        ErrorCode errorCode = businessException.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, businessException.getDetail());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(errorResponse);
    }

    public static class ErrorResponse {
        private final String message;
        private final String detail;

        public ErrorResponse(String message, String detail) {
            this.message = message;
            this.detail = detail;
        }

        public static ErrorResponse of(ErrorCode errorCode, String detail) {
            return new ErrorResponse(errorCode.getMessage(), detail);
        }

        public String getDetail() {
            return detail;
        }

        public String getMessage() {
            return message;
        }
    }
}
