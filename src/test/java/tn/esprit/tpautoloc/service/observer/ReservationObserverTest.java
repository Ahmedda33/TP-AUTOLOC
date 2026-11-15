package tn.esprit.tpautoloc.service.observer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.tpautoloc.domain.Client;
import tn.esprit.tpautoloc.domain.Reservation;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class ReservationObserverTest {

    @Autowired
    private ReservationSubject reservationSubject;

    @Test
    void testNotifierConfirmation() {
        Client client = new Client();
        client.setEmail("test@test.com");
        client.setNom("Test");
        client.setTelephone("99000000");

        Reservation reservation = new Reservation();
        reservation.setDateDebut(LocalDate.now());
        reservation.setDateFin(LocalDate.now().plusDays(3));
        reservation.setClient(client);

        assertDoesNotThrow(() -> reservationSubject.notifierConfirmation(reservation),
                "La notification de confirmation ne doit pas lever d'exception");
    }

    @Test
    void testNotifierAnnulation() {
        Client client = new Client();
        client.setEmail("test@test.com");
        client.setTelephone("99000000");

        Reservation reservation = new Reservation();
        reservation.setDateDebut(LocalDate.now());
        reservation.setDateFin(LocalDate.now().plusDays(2));
        reservation.setClient(client);

        assertDoesNotThrow(() -> reservationSubject.notifierAnnulation(reservation),
                "La notification d'annulation ne doit pas lever d'exception");
    }
}
