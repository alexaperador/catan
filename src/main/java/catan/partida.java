package catan;

import java.lang.annotation.Inherited;

import javax.annotation.processing.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Partida")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(lenth = 30)
public abstract class partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull(message = "El estat és obligatori")
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", nullable = false, length = 10)
    private estatPartida estat;
    
    @NotNull(message = "La data d'inici és obligatòria")
	@PastOrPresent(message = "La data de llançament no pot ser futura")
	@Column(name = "data_inici", nullable = false)
    private LocalDate dataInici;

    @Min(value = 1)
    @Column(name = "ronda")
    private Integer ronda;

    @Column(name = "ultima_tirada")
    private Integer ultimaTirada;

    @Def
    @Column(name = "punts_per_guanyar")
    private Integer PuntsPerGuanyar;

    @Column(name = "daus_tirats")
    private Boolean dausTirats;

    private Boolean lladrePendent;

}