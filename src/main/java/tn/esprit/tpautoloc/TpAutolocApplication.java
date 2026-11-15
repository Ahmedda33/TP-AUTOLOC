package tn.esprit.tpautoloc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tn.esprit.tpautoloc.domain.Contrat;
import tn.esprit.tpautoloc.domain.Paiement;
import tn.esprit.tpautoloc.domain.Vehicule;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;
import tn.esprit.tpautoloc.repository.IContratRepository;
import tn.esprit.tpautoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@SpringBootApplication
public class TpAutolocApplication {

    private static final Logger log = LoggerFactory.getLogger(TpAutolocApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TpAutolocApplication.class, args);
    }

    @Bean
    public CommandLineRunner initVehicules(IVehiculeRepository vehiculeRepository) {
        return args -> {
            if (vehiculeRepository.count() > 0) {
                return;
            }
            Vehicule v1 = new Vehicule(null, "123-TU-456", "Renault", "Clio 4", CategorieVehicule.CITADINE, new BigDecimal("80.0"), StatutVehicule.DISPONIBLE, null, null, null, null);
            Vehicule v2 = new Vehicule(null, "789-TU-101", "Peugeot", "208", CategorieVehicule.CITADINE, new BigDecimal("85.0"), StatutVehicule.LOUE, null, null, null, null);
            Vehicule v3 = new Vehicule(null, "111-TU-222", "Volkswagen", "Tiguan", CategorieVehicule.SUV, new BigDecimal("150.0"), StatutVehicule.MAINTENANCE, null, null, null, null);

            vehiculeRepository.save(v1);
            vehiculeRepository.save(v2);
            vehiculeRepository.save(v3);
        };
    }

    @Bean
    public CommandLineRunner demoContrat(IContratRepository contratRepository) {
        return args -> {
            Contrat contrat = new Contrat();
            contrat.setDateSignature(LocalDate.now());
            contrat.setMontantTotal(new BigDecimal("240.0"));
            contrat.setValide(true);
            contrat.setPaiements(new ArrayList<>());

            Paiement acompte = new Paiement();
            acompte.setMontant(new BigDecimal("100.0"));
            acompte.setDatePaiement(LocalDate.now());
            acompte.setContrat(contrat);
            contrat.getPaiements().add(acompte);

            Paiement solde = new Paiement();
            solde.setMontant(new BigDecimal("140.0"));
            solde.setDatePaiement(LocalDate.now());
            solde.setContrat(contrat);
            contrat.getPaiements().add(solde);

            Contrat enregistre = contratRepository.save(contrat);
            Long id = enregistre.getIdContrat();
            log.info("Contrat créé, id = {}", id);

            // 2. findById : renvoie un Optional
            Optional<Contrat> trouve = contratRepository.findById(id);
            log.info("Contrat trouvé ? {}", trouve.isPresent());

            // 3. save avec id renseigné : SELECT puis UPDATE
            enregistre.setMontantTotal(new BigDecimal("260.0"));
            enregistre = contratRepository.save(enregistre);

            enregistre.getPaiements().remove(0);
            enregistre = contratRepository.save(enregistre);

            // 5. deleteById : supprime le contrat et le paiement restant (cascade REMOVE)
            contratRepository.deleteById(id);
            log.info("Contrat {} existe encore ? {}", id, contratRepository.existsById(id));
        };
    }
}
