package tn.esprit.tpautoloc.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Table d'association Vehicule <-> Equipement (remplace l'ancien @ManyToMany + @JoinTable).
 */
@Entity
@Table(name = "vehicule_equipement")
@Getter
@Setter
@NoArgsConstructor
public class VehiculeEquipement {

    @EmbeddedId
    private VehiculeEquipementId id = new VehiculeEquipementId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idVehicule")
    @JoinColumn(name = "id_vehicule")
    @JsonBackReference("vehicule-vehiculeEquipements")
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idEquipement")
    @JoinColumn(name = "id_equipement")
    @JsonBackReference("equipement-vehiculeEquipements")
    private Equipement equipement;

    public VehiculeEquipement(Vehicule vehicule, Equipement equipement) {
        this.vehicule = vehicule;
        this.equipement = equipement;
    }
}
