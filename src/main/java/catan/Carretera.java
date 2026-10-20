package catan;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

    

@Entity
@Table(name = "Carretera")
@Inheritance(strategy = InheritanceType.JOINED)



public class Carretera extends Construccio{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Aresta")

    Aresta aresta;
}