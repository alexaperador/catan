package catan;

import java.lang.annotation.Inherited;
import java.time.LocalDate;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "HexagonVertex")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(length = 30)
public abstract class HexagonVertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @NotNull(message = "La pocisio és obligatoria")
    @Column(name = "posicio", nullable = false)
    private int posicio;

    @OneToMany(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hexagon_id", nullable = false)
	private Hexagon hexagon;

    @OneToMany(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vertex_id", nullable = false)
	private Vertex vertex;

}