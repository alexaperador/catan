package catan;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Vertex")
@Inheritance(strategy = InheritanceType.JOINED)



public class Vertex extends Assentament{
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Vertex")
    int id;

    @NotNull(message = "El codi és obligatori")
    int codi;

    @OneToMany(mappedBy = "hexagon", cascade = CascadeType.ALL)
    private List<HexagonVertex> HexagonVertexs = new ArrayList<>();

    protected Vertex(){

    }

    public Vertex (int codi, List<HexagonVertex> HexagonVertexs){
        this.codi = codi;
        this.HexagonVertexs = HexagonVertexs;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(int codi) {
        this.codi = codi;
    }

    public int getId() {
        return id;
    }

    public List<HexagonVertex> getHexagonVertexs() {
        return HexagonVertexs;
    }

    public void setHexagonVertexs(List<HexagonVertex> HexagonVertexs) {
        this.HexagonVertexs = HexagonVertexs;
    }

}