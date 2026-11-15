package tn.esprit.tpautoloc.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "equipement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEquipement;
    private String libelle;

    @OneToMany(mappedBy = "equipement", cascade = CascadeType.ALL)
    @JsonManagedReference("equipement-vehiculeEquipements")
    private List<VehiculeEquipement> vehiculeEquipements;
}
