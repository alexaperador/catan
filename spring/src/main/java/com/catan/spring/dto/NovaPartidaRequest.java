package com.catan.spring.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cos de POST /partida. Exemple:
 * { "jugadors": ["Anna", "Biel", "Carla"], "puntsPerGuanyar": 10 }
 * puntsPerGuanyar és opcional (per defecte 10).
 */
public record NovaPartidaRequest(
        @NotNull(message = "cal la llista de jugadors")
        @Size(min = 2, max = 4, message = "hi ha d'haver entre 2 i 4 jugadors")
        List<@NotBlank(message = "el nom no pot estar buit") @Size(max = 50) String> jugadors,
        @Min(value = 1, message = "ha de ser com a mínim 1") Integer puntsPerGuanyar) {
}
