package tn.esprit.tpautoloc.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import tn.esprit.tpautoloc.domain.enums.StatutReservation;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReservation;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut;

    @ManyToOne
    @JoinColumn(name = "id_client")
    @JsonBackReference("client-reservations")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "id_vehicule")
    @JsonBackReference("vehicule-reservations")
    private Vehicule vehicule;

    @OneToOne(mappedBy = "reservation")
    @JsonBackReference("contrat-reservation")
    private Contrat contrat;
}
