package tn.esprit.tpautoloc.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;

@Entity
@Table(name = "vehicule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVehicule;
    private String immatriculation;
    private String marque;
    private String modele;

    @Enumerated(EnumType.STRING)
    private CategorieVehicule categorie;

    private BigDecimal tarifJournalier;

    @Enumerated(EnumType.STRING)
    private StatutVehicule statut;

    @ManyToOne
    @JoinColumn(name = "id_agence")
    @JsonBackReference("agence-vehicules")
    private Agence agence;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL)
    @JsonManagedReference("vehicule-reservations")
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL)
    @JsonManagedReference("vehicule-maintenances")
    private List<Maintenance> maintenances;

    @OneToMany(mappedBy = "vehicule", cascade = CascadeType.ALL)
    @JsonManagedReference("vehicule-vehiculeEquipements")
    private List<VehiculeEquipement> vehiculeEquipements;
}
