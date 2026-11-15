package tn.esprit.tpautoloc.service.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.esprit.tpautoloc.domain.Reservation;

@Slf4j
@Component
public class EmailNotificationObserver implements IReservationObserver {

    @Override
    public void onReservationConfirmee(Reservation reservation) {
        String email = (reservation.getClient() != null) ? reservation.getClient().getEmail() : "inconnu";
        log.info("[EMAIL] Confirmation envoyée au client {} pour la réservation #{}",
                email, reservation.getIdReservation());
    }

    @Override
    public void onReservationAnnulee(Reservation reservation) {
        String email = (reservation.getClient() != null) ? reservation.getClient().getEmail() : "inconnu";
        log.info("[EMAIL] Annulation notifiée au client {} pour la réservation #{}",
                email, reservation.getIdReservation());
    }
}
