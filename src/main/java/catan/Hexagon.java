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
@Table(name = "Hexagon")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(length = 30)
public abstract class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codi")
    private Integer codi;

    @NotNull(message = "El tipus de terreny és obligatori")
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", nullable = false, length = 15)
    private TipusTerreny terreny;

    @Min(2)
    @Max(12)
    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Column(name = "q")
    private int q;

    @Column(name = "r")
    private int r;

    @OneToMany(mappedBy = "hexagon", cascade = CascadeType.ALL)
    private List<HexagonVertex> HexagonVertexs = new ArrayList<>();

}