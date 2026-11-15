package tn.esprit.tpautoloc.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.ModePaiement;
import tn.esprit.tpautoloc.web.dto.AgenceDTO;
import tn.esprit.tpautoloc.web.dto.ClientDTO;
import tn.esprit.tpautoloc.web.dto.PaiementDTO;
import tn.esprit.tpautoloc.web.dto.ReservationRequestDTO;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Atelier 7 – Partie 5.6 : règles de Bean Validation sur les DTO (test unitaire, sans base). */
@DisplayName("Bean Validation sur les DTO")
class ValidationDtoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private static boolean violeChamp(Set<? extends ConstraintViolation<?>> violations, String champ) {
        return violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(champ));
    }

    @Test
    @DisplayName("Client avec un email invalide : rejeté")
    void clientEmailInvalide() {
        ClientDTO client = ClientDTO.builder().nom("Ben Ali").prenom("Sami").email("pas-un-email").build();
        assertTrue(violeChamp(validator.validate(client), "email"));
    }

    @Test
    @DisplayName("Client valide : accepté")
    void clientValide() {
        ClientDTO client = ClientDTO.builder().nom("Ben Ali").prenom("Sami").email("sami@autoloc.tn").build();
        assertTrue(validator.validate(client).isEmpty());
    }

    @Test
    @DisplayName("Agence sans nom ni ville : rejetée")
    void agenceSansNom() {
        var violations = validator.validate(new AgenceDTO());
        assertTrue(violeChamp(violations, "nom"));
        assertTrue(violeChamp(violations, "ville"));
    }

    @Test
    @DisplayName("Véhicule sans immatriculation : rejeté ; tarif nul ou négatif : rejeté")
    void vehiculeInvalide() {
        VehiculeDTO vehicule = VehiculeDTO.builder().marque("Kia").modele("Rio")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("50")).agenceId(1L).build();
        assertTrue(violeChamp(validator.validate(vehicule), "immatriculation"));

        vehicule.setImmatriculation("123-TU-456");
        assertTrue(validator.validate(vehicule).isEmpty());

        vehicule.setTarifJournalier(BigDecimal.ZERO);
        assertTrue(violeChamp(validator.validate(vehicule), "tarifJournalier"));
        vehicule.setTarifJournalier(new BigDecimal("-1"));
        assertTrue(violeChamp(validator.validate(vehicule), "tarifJournalier"));
    }

    @Test
    @DisplayName("Paiement avec un montant nul ou négatif : rejeté")
    void paiementMontantInvalide() {
        PaiementDTO paiement = PaiementDTO.builder().montant(BigDecimal.ZERO)
                .modePaiement(ModePaiement.CARTE).contratId(1L).build();
        assertTrue(violeChamp(validator.validate(paiement), "montant"));

        paiement.setMontant(new BigDecimal("-5"));
        assertTrue(violeChamp(validator.validate(paiement), "montant"));

        paiement.setMontant(new BigDecimal("100"));
        assertTrue(validator.validate(paiement).isEmpty());
    }

    @Test
    @DisplayName("Réservation sans date de début ou de fin : rejetée")
    void reservationSansDates() {
        ReservationRequestDTO dto = ReservationRequestDTO.builder().clientId(1L).vehiculeId(1L).build();
        var violations = validator.validate(dto);
        assertTrue(violeChamp(violations, "dateDebut"));
        assertTrue(violeChamp(violations, "dateFin"));
    }

    @Test
    @DisplayName("Réservation avec une date de début passée : rejetée")
    void reservationDateDebutPassee() {
        ReservationRequestDTO dto = ReservationRequestDTO.builder().clientId(1L).vehiculeId(1L)
                .dateDebut(LocalDate.now().minusDays(1)).dateFin(LocalDate.now().plusDays(2)).build();
        assertTrue(violeChamp(validator.validate(dto), "dateDebut"));
    }

    @Test
    @DisplayName("Réservation cohérente : acceptée")
    void reservationValide() {
        ReservationRequestDTO dto = ReservationRequestDTO.builder().clientId(1L).vehiculeId(1L)
                .dateDebut(LocalDate.now().plusDays(1)).dateFin(LocalDate.now().plusDays(4)).build();
        assertEquals(0, validator.validate(dto).size());
    }
}
