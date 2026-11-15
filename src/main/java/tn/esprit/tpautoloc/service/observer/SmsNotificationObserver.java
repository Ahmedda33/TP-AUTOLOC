package tn.esprit.tpautoloc.service.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.esprit.tpautoloc.domain.Reservation;

@Slf4j
@Component
public class SmsNotificationObserver implements IReservationObserver {

    @Override
    public void onReservationConfirmee(Reservation reservation) {
        String telephone = (reservation.getClient() != null) ? reservation.getClient().getTelephone() : "inconnu";
        log.info("[SMS] Confirmation envoyée au {} pour la réservation #{}",
                telephone, reservation.getIdReservation());
    }

    @Override
    public void onReservationAnnulee(Reservation reservation) {
        String telephone = (reservation.getClient() != null) ? reservation.getClient().getTelephone() : "inconnu";
        log.info("[SMS] Annulation notifiée au {} pour la réservation #{}",
                telephone, reservation.getIdReservation());
    }
}
