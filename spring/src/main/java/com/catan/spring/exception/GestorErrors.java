package com.catan.spring.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converteix les excepcions dels serveis en respostes HTTP amb el missatge com a String,
 * així els controladors no han de repetir try/catch.
 */
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
        String missatge = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("Petició no vàlida");
        return ResponseEntity.badRequest().body(missatge);
    }
}
