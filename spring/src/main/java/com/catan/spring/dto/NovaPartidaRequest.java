package com.catan.spring.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaPartidaRequest(
        @NotNull(message = "La llista de jugadors no pot ser null") 
        @Size(min = 2, max = 4, message = "La partida ha de tenir entre 2 i 4 jugadors") 
        List<@NotBlank(message = "El nom del jugador no pot ser buit") @Size(max = 50) String> jugadors,
                  
        @Min(value = 1, message = "El nombre de punts per guanyar ha de ser com a mínim 1") 
        Integer puntsPerGuanyar) {
}
