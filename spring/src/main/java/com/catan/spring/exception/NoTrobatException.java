package com.catan.spring.exception;

/** La partida o el jugador demanat no existeix (resposta 404). */
public class NoTrobatException extends RuntimeException {
    public NoTrobatException(String missatge) {
        super(missatge);
    }
}
