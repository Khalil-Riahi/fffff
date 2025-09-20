/* RestExceptionHandler.java */
package com.projet.freelencetinder.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class RestExceptionHandler {

    record ErrorDto(String message) {}
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(ex.getMessage()));
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> business(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDto(ex.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> illegalArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorDto(ex.getMessage()));
    }
    @ExceptionHandler(PaymeeApiException.class)
    public ResponseEntity<ErrorDto> paymee(PaymeeApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorDto(ex.getMessage()));
    }
}
