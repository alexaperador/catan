package com.catan.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestorErrors {

    @ExceptionHandler(NoTrobatException.class)
    public ResponseEntity<String> noTrobat(NoTrobatException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ReglaException.class)
    public ResponseEntity<String> regla(ReglaException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> validacio(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
