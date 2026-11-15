package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.StatutReservation;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;
import tn.esprit.tpautoloc.web.dto.AgenceDTO;
import tn.esprit.tpautoloc.web.dto.ClientDTO;
import tn.esprit.tpautoloc.web.dto.ReservationRequestDTO;
import tn.esprit.tpautoloc.web.dto.ReservationResponseDTO;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 4/6/7 : test d'intégration du service Reservation (ReservationRequestDTO / ReservationResponseDTO).
 * La création déclenche les observateurs (logs [EMAIL] et [SMS]) ; l'annulation aussi.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Reservation – tests d'intégration")
class ReservationServiceTest {

    @Autowired
    private IReservationService reservationService;
    @Autowired
    private IClientService clientService;
    @Autowired
    private IVehiculeService vehiculeService;
    @Autowired
    private IAgenceService agenceService;

    private Long clientId;
    private Long vehiculeId;

    private void preparerClientEtVehicule() {
        clientId = clientService.create(ClientDTO.builder().nom("Ben Ali").prenom("Sami")
                .email("sami.benali@autoloc.tn").telephone("20111111").build()).getId();
        Long agenceId = agenceService.create(AgenceDTO.builder().nom("Agence Test").ville("Tunis").build()).getId();
        vehiculeId = vehiculeService.create(VehiculeDTO.builder().immatriculation("123-TU-456").marque("Renault")
                .modele("Clio").categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("80.0"))
                .statut(StatutVehicule.DISPONIBLE).agenceId(agenceId).build()).getId();
    }

    private ReservationRequestDTO requete(int dansNJours, int dureeJours) {
        LocalDate debut = LocalDate.now().plusDays(dansNJours);
        return ReservationRequestDTO.builder().clientId(clientId).vehiculeId(vehiculeId)
                .dateDebut(debut).dateFin(debut.plusDays(dureeJours)).build();
    }

    @Test
    @DisplayName("Reservation : create, findAll, findById, existsById, update, deleteById")
    void testReservationService() {
        preparerClientEtVehicule();

        ReservationResponseDTO saved = reservationService.create(requete(1, 5));
        assertNotNull(saved.getId());
        assertEquals(StatutReservation.CONFIRMEE, saved.getStatut());
        assertEquals(clientId, saved.getClientId());
        assertEquals("Sami Ben Ali", saved.getClientNomComplet());
        assertEquals(vehiculeId, saved.getVehiculeId());
        assertEquals("123-TU-456", saved.getVehiculeImmatriculation());
        assertNotNull(saved.getTarifEstime(), "le tarif estime vient de la strategie de tarification");

        assertTrue(reservationService.findAll().stream().anyMatch(r -> r.getId().equals(saved.getId())));
        assertEquals(saved.getId(), reservationService.findById(saved.getId()).getId());
        assertTrue(reservationService.existsById(saved.getId()));

        ReservationResponseDTO modifiee = reservationService.update(saved.getId(), requete(2, 3));
        assertEquals(LocalDate.now().plusDays(2), modifiee.getDateDebut());

        reservationService.deleteById(saved.getId());
        assertFalse(reservationService.existsById(saved.getId()));
        assertThrows(ResourceNotFoundException.class, () -> reservationService.findById(saved.getId()));
    }

    @Test
    @DisplayName("Reservation : annulerReservation passe le statut a ANNULEE")
    void testAnnulation() {
        preparerClientEtVehicule();
        ReservationResponseDTO saved = reservationService.create(requete(1, 2));

        ReservationResponseDTO annulee = reservationService.annulerReservation(saved.getId());
        assertEquals(StatutReservation.ANNULEE, annulee.getStatut());
        assertEquals(StatutReservation.ANNULEE, reservationService.findById(saved.getId()).getStatut());
        assertThrows(ResourceNotFoundException.class, () -> reservationService.annulerReservation(-1L));
    }

    @Test
    @DisplayName("Reservation : dates incoherentes, client ou vehicule inexistant refuses")
    void testReglesReservation() {
        preparerClientEtVehicule();

        ReservationRequestDTO datesInversees = requete(5, 1);
        datesInversees.setDateFin(datesInversees.getDateDebut().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> reservationService.create(datesInversees));

        ReservationRequestDTO clientInconnu = requete(1, 2);
        clientInconnu.setClientId(-1L);
        assertThrows(ResourceNotFoundException.class, () -> reservationService.create(clientInconnu));

        ReservationRequestDTO vehiculeInconnu = requete(1, 2);
        vehiculeInconnu.setVehiculeId(-1L);
        assertThrows(ResourceNotFoundException.class, () -> reservationService.create(vehiculeInconnu));
    }
}
