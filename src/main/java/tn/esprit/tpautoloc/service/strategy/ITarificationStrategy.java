package tn.esprit.tpautoloc.service.strategy;

import java.math.BigDecimal;
import tn.esprit.tpautoloc.domain.Reservation;
import tn.esprit.tpautoloc.domain.Vehicule;

/**
 * Patron GoF : STRATEGY.
 * Définit le contrat pour le calcul du tarif de location.
 */
public interface ITarificationStrategy {
    BigDecimal calculerTarif(Vehicule vehicule, Reservation reservation);
    String getCode();
}
