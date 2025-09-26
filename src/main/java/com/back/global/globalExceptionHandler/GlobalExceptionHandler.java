package com.back.global.globalExceptionHandler;


import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 글로벌 예외 핸들러 클래스
 * 각 예외에 대한 적절한 HTTP 상태 코드와 메시지를 포함한 응답 반환
 * 400: Bad Request
 * 404: Not Found
 * 500: Internal Server Error
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ServiceException: 서비스 계층에서 발생하는 커스텀 예외
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<RsData<Void>> handle(ServiceException ex) {
        RsData<Void> rsData = ex.getRsData();
        int statusCode = rsData.code();

        HttpStatus status = HttpStatus.resolve(statusCode);
        if( status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 기본값 설정
        }
        return ResponseEntity.status(status).body(rsData);

    }

    // NoSuchElementException: 데이터 없을떄 예외
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<RsData<Void>> handle(NoSuchElementException ex) {
        return new ResponseEntity<>(
                RsData.of(
                        404,
                        "해당 데이터가 존재하지 않습니다"
                ),
                NOT_FOUND
        );
    }

    //ConstraintViolationException: 제약 조건(@NotNull, @Size 등)을 어겼을 때 발생예외
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handle(ConstraintViolationException ex) {
        //메세지 형식 : <필드명>-<검증어노테이션명>-<검증실패메시지>
        String message = ex.getConstraintViolations()
                .stream()
                .map(
                        violation -> {
                            String path  = violation.getPropertyPath().toString();
                            String field = path.contains(".") ? path.split("\\.",2)[1]: path;
                            String[] messageTemplateBits = violation.getMessageTemplate()
                                    .split("\\.");
                            String code = messageTemplateBits.length >= 2
                            ? messageTemplateBits[messageTemplateBits.length -2] : "Unknown";

                            String _message = violation.getMessage();

                            return "%s-%s-%s".formatted(field, code, _message);
                        })
                .sorted()
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }


    // MethodArgumentNotValidException: @Valid 어노테이션을 사용한 유효성 검사 실패시 발생하는 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handle(MethodArgumentNotValidException ex) {
        //메세지 형식 : <필드명>-<검증어노테이션명>-<검증실패메시지>
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
                .sorted(Comparator.comparing(String::toString))
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ) ,
                BAD_REQUEST
        );
    }

    // HttpMessageNotReadableException : 요청 본문이 올바르지 않을 때 발생하는 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RsData<Void>> handle(HttpMessageNotReadableException ex) {
        return new ResponseEntity<>(
                RsData.of(
                        400,
                        "요청 본문이 올바르지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    // MissingRequestHeaderException : 필수 요청 헤더가 누락되었을 때 발생하는 예외
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<RsData<Void>> handle(MissingRequestHeaderException ex) {
        // 메세지 형식 : <필드명>-<검증어노테이션명>-<검증실패메시지>
        String message = "%s-%s-%s".formatted(
                ex.getHeaderName(),
                "NotBlank",
                ex.getLocalizedMessage()
        );

        return new ResponseEntity<>(
                RsData.of(
                        400,
                        message
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<RsData<Void>> handleJsonProcessingException(JsonProcessingException e) {
        return ResponseEntity.badRequest()
                .body(RsData.of(400, "JSON 파싱 오류가 발생했습니다.", null));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<RsData<Void>> handleIOException(IOException e) {
        return ResponseEntity.internalServerError()
                .body(RsData.of(500, "서버 내부 오류가 발생했습니다.", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RsData.of(404, ex.getMessage()));
    }
}
