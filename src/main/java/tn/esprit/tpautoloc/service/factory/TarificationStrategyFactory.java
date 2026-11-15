package tn.esprit.tpautoloc.service.factory;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.esprit.tpautoloc.domain.Reservation;
import tn.esprit.tpautoloc.service.strategy.ITarificationStrategy;

/**
 * Patron GoF : FACTORY METHOD appliqué aux stratégies de tarification.
 */
@Slf4j
@Component
public class TarificationStrategyFactory {

    private final Map<String, ITarificationStrategy> strategies;

    public TarificationStrategyFactory(List<ITarificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(ITarificationStrategy::getCode, Function.identity()));
        log.info("TarificationStrategyFactory : {} stratégies chargées : {}",
                strategies.size(), strategies.keySet());
    }

    public ITarificationStrategy resoudre(Reservation reservation) {
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            return strategies.get("STANDARD");
        }
        long jours = ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin());
        if (jours >= 7) {
            return strategies.getOrDefault("LONGUE_DUREE", strategies.get("STANDARD"));
        }
        if (estWeekend(reservation)) {
            return strategies.getOrDefault("WEEKEND", strategies.get("STANDARD"));
        }
        return strategies.getOrDefault("STANDARD", strategies.values().iterator().next());
    }

    private boolean estWeekend(Reservation reservation) {
        DayOfWeek jour = reservation.getDateDebut().getDayOfWeek();
        return jour == DayOfWeek.SATURDAY || jour == DayOfWeek.SUNDAY;
    }
}
