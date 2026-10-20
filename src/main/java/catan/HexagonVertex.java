package catan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "HexagonVertex")
public class HexagonVertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "hexagon_id", nullable = false)
    private Hexagon hexagon;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;

    @Min(0)
    @Max(5)
    @Column(name = "posicio", nullable = false)
    private int posicio;

}