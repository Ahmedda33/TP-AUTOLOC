package tn.esprit.tpautoloc.service.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.tpautoloc.domain.Reservation;
import tn.esprit.tpautoloc.service.strategy.TarifLongueDureeStrategy;
import tn.esprit.tpautoloc.service.strategy.TarifStandardStrategy;
import tn.esprit.tpautoloc.service.strategy.TarifWeekendStrategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Atelier 6 – Factory Test : test unitaire (sans Spring) du choix de la stratégie de tarification.
 * Cas couverts : 7 j (mercredi), 7 j (borne), 2 j commençant un vendredi, 4 j (lundi).
 */
@DisplayName("TarificationStrategyFactory – choix de la stratégie")
class TarificationStrategyFactoryTest {

    private TarificationStrategyFactory factory;

    @BeforeEach
    void setUp() {
        TarifStandardStrategy standard = new TarifStandardStrategy();
        TarifWeekendStrategy weekend = new TarifWeekendStrategy();
        TarifLongueDureeStrategy longueDuree = new TarifLongueDureeStrategy();
        factory = new TarificationStrategyFactory(List.of(standard, weekend, longueDuree));
    }

    private Reservation reservation(LocalDate debut, LocalDate fin) {
        Reservation reservation = new Reservation();
        reservation.setDateDebut(debut);
        reservation.setDateFin(fin);
        return reservation;
    }

    @Test
    @DisplayName("7 jours ou plus -> LONGUE_DUREE")
    void doitRetournerLongueDureePour7JoursOuPlus() {
        var resultat = factory.resoudre(reservation(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)));

        assertInstanceOf(TarifLongueDureeStrategy.class, resultat);
        assertEquals("LONGUE_DUREE", resultat.getCode());
    }

    @Test
    @DisplayName("borne exacte à 7 jours -> LONGUE_DUREE")
    void doitRetournerLongueDureePourExactement7Jours() {
        var resultat = factory.resoudre(reservation(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 8)));

        assertEquals("LONGUE_DUREE", resultat.getCode());
    }

    @Test
    @DisplayName("court séjour commençant un vendredi -> WEEKEND")
    void doitRetournerWeekendPourCourtSejourVendredi() {
        LocalDate vendredi = LocalDate.of(2024, 3, 8);
        assertEquals(DayOfWeek.FRIDAY, vendredi.getDayOfWeek(), "le 2024-03-08 doit être un vendredi");

        var resultat = factory.resoudre(reservation(vendredi, LocalDate.of(2024, 3, 10)));

        assertInstanceOf(TarifWeekendStrategy.class, resultat);
        assertEquals("WEEKEND", resultat.getCode());
    }

    @Test
    @DisplayName("durée moyenne (4 à 6 jours) -> STANDARD")
    void doitRetournerStandardPourMoyenneDuree() {
        var resultat = factory.resoudre(reservation(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8)));

        assertEquals("STANDARD", resultat.getCode());
    }
}
