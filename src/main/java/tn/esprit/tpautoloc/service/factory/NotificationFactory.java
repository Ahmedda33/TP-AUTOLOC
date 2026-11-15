package tn.esprit.tpautoloc.service.factory;

import org.springframework.stereotype.Component;
import tn.esprit.tpautoloc.service.observer.EmailNotificationObserver;
import tn.esprit.tpautoloc.service.observer.IReservationObserver;
import tn.esprit.tpautoloc.service.observer.SmsNotificationObserver;

/**
 * Patron GoF : FACTORY METHOD appliqué aux observateurs de notification.
 */
@Component
public class NotificationFactory {

    private final EmailNotificationObserver emailObserver;
    private final SmsNotificationObserver smsObserver;

    public NotificationFactory(EmailNotificationObserver emailObserver,
                               SmsNotificationObserver smsObserver) {
        this.emailObserver = emailObserver;
        this.smsObserver = smsObserver;
    }

    public IReservationObserver creerObservateur(String canal) {
        if ("SMS".equalsIgnoreCase(canal)) {
            return smsObserver;
        }
        return emailObserver;
    }
}
