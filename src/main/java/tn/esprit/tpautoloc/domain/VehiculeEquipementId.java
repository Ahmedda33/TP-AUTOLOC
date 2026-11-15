package tn.esprit.tpautoloc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VehiculeEquipementId implements Serializable {

    @Column(name = "id_vehicule")
    private Long idVehicule;

    @Column(name = "id_equipement")
    private Long idEquipement;
}
