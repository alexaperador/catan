package com.catan.spring.exception;

/** Una acció no es pot fer perquè incompleix una regla del joc (resposta 400). */
public class ReglaException extends RuntimeException {
    public ReglaException(String missatge) {
        super(missatge);
    }
}
