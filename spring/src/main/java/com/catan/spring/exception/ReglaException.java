package com.catan.spring.exception;

public class ReglaException extends RuntimeException {
    public ReglaException(String missatge) {
        super(missatge);
    }
}
