package com.shootdoori.match.exception.handler;

import com.shootdoori.match.exception.common.BusinessException;
import com.shootdoori.match.exception.common.ErrorCode;
import com.shootdoori.match.exception.common.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException e
    ) {
        log.warn("[GlobalExceptionHandler] BusinessException: {}", e.getErrorCode());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, e.getDetail());

        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("[GlobalExceptionHandler] UnauthorizedException: {}", e.getErrorCode());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.name(),
                e.getDetail() != null ? e.getDetail() : errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String fieldError = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "잘못된 요청입니다.";
        log.warn("[GlobalExceptionHandler] Validation error: {}", fieldError);
        ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", fieldError);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[GlobalExceptionHandler] IllegalArgumentException");
        ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        log.error("[GlobalExceptionHandler] Unexpected Exception occurred", e);
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected server error occurred.");
        return ResponseEntity.internalServerError().body(response);
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
