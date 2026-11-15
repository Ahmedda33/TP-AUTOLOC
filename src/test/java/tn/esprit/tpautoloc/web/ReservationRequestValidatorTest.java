package tn.esprit.tpautoloc.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import tn.esprit.tpautoloc.web.dto.ReservationRequestDTO;
import tn.esprit.tpautoloc.web.validator.ReservationRequestValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Atelier 7 – Partie 5.4/5.6 : validateur personnalisé de la cohérence des dates. */
@DisplayName("ReservationRequestValidator")
class ReservationRequestValidatorTest {

    private final ReservationRequestValidator validator = new ReservationRequestValidator();

    private Errors valider(LocalDate debut, LocalDate fin) {
        ReservationRequestDTO dto = ReservationRequestDTO.builder().clientId(1L).vehiculeId(1L)
                .dateDebut(debut).dateFin(fin).build();
        Errors errors = new BeanPropertyBindingResult(dto, "reservation");
        validator.validate(dto, errors);
        return errors;
    }

    @Test
    @DisplayName("supports() ne reconnaît que ReservationRequestDTO")
    void supports() {
        assertTrue(validator.supports(ReservationRequestDTO.class));
        assertFalse(validator.supports(String.class));
    }

    @Test
    @DisplayName("dates cohérentes : aucune erreur")
    void datesCoherentes() {
        assertFalse(valider(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)).hasErrors());
        assertFalse(valider(LocalDate.now(), LocalDate.now().plusDays(1)).hasErrors(),
                "un début aujourd'hui est accepté");
    }

    @Test
    @DisplayName("fin antérieure au début : erreur sur dateFin")
    void finAnterieure() {
        Errors errors = valider(LocalDate.now().plusDays(5), LocalDate.now().plusDays(2));
        assertEquals(1, errors.getErrorCount());
        assertNotNull(errors.getFieldError("dateFin"));
    }

    @Test
    @DisplayName("fin égale au début : erreur sur dateFin")
    void finEgaleAuDebut() {
        LocalDate jour = LocalDate.now().plusDays(2);
        assertNotNull(valider(jour, jour).getFieldError("dateFin"));
    }

    @Test
    @DisplayName("début dans le passé : erreur sur dateDebut")
    void debutPasse() {
        Errors errors = valider(LocalDate.now().minusDays(1), LocalDate.now().plusDays(2));
        assertNotNull(errors.getFieldError("dateDebut"));
    }

    @Test
    @DisplayName("dates absentes : ignorées (traitées par Bean Validation)")
    void datesAbsentes() {
        assertFalse(valider(null, null).hasErrors());
        assertFalse(valider(LocalDate.now().plusDays(1), null).hasErrors());
    }
}
