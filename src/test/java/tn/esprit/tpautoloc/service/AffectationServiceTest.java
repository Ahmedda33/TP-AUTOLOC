package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.domain.Employe;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.RoleEmploye;
import tn.esprit.tpautoloc.repository.IEmployeRepository;
import tn.esprit.tpautoloc.web.dto.AgenceDTO;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 7 – Partie 1 : affectation / désaffectation d'un véhicule et d'un employé à une agence.
 * Chaque test est transactionnel (@Rollback) : aucune donnée ne reste en base.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Affectation / désaffectation à une agence")
class AffectationServiceTest {

    @Autowired
    private IVehiculeService vehiculeService;
    @Autowired
    private IEmployeService employeService;
    @Autowired
    private IAgenceService agenceService;
    @Autowired
    private IEmployeRepository employeRepository;

    private Long creerAgence(String nom) {
        return agenceService.create(AgenceDTO.builder().nom(nom).ville("Tunis").build()).getId();
    }

    private Long creerVehiculeSansAgence() {
        // Le service accepte un véhicule sans agence : l'agence est affectée ensuite.
        return vehiculeService.create(VehiculeDTO.builder().immatriculation("555-TU-555").marque("Kia")
                .modele("Rio").categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal("60.0"))
                .build()).getId();
    }

    private Long creerEmploye() {
        Employe e = new Employe();
        e.setNom("Gharbi");
        e.setPrenom("Mehdi");
        e.setRole(RoleEmploye.AGENT);
        return employeService.create(e).getIdEmploye();
    }

    // ----------------------------- Véhicule -----------------------------

    @Test
    @DisplayName("Véhicule : affectation à une agence existante")
    void affecterVehicule() {
        Long vehiculeId = creerVehiculeSansAgence();
        Long agenceId = creerAgence("Agence A");

        vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceId);

        assertEquals(agenceId, vehiculeService.findById(vehiculeId).getAgenceId());
    }

    @Test
    @DisplayName("Véhicule : véhicule puis agence inexistants -> ResourceNotFoundException")
    void affecterVehiculeInexistant() {
        Long vehiculeId = creerVehiculeSansAgence();
        Long agenceId = creerAgence("Agence A");

        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.affecterVehiculeAAgence(-1L, agenceId));
        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.affecterVehiculeAAgence(vehiculeId, -1L));
        assertNull(vehiculeService.findById(vehiculeId).getAgenceId(), "l'affectation refusée ne change rien");
    }

    @Test
    @DisplayName("Véhicule : transfert d'une agence vers une autre")
    void transfererVehicule() {
        Long vehiculeId = creerVehiculeSansAgence();
        Long agenceA = creerAgence("Agence A");
        Long agenceB = creerAgence("Agence B");

        vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceA);
        vehiculeService.affecterVehiculeAAgence(vehiculeId, agenceB);

        assertEquals(agenceB, vehiculeService.findById(vehiculeId).getAgenceId());
    }

    @Test
    @DisplayName("Véhicule : désaffectation (clé étrangère à null) et refus si aucune agence")
    void desaffecterVehicule() {
        Long vehiculeId = creerVehiculeSansAgence();
        vehiculeService.affecterVehiculeAAgence(vehiculeId, creerAgence("Agence A"));

        vehiculeService.desaffecterVehiculeDeAgence(vehiculeId);
        assertNull(vehiculeService.findById(vehiculeId).getAgenceId());

        assertThrows(IllegalArgumentException.class, () -> vehiculeService.desaffecterVehiculeDeAgence(vehiculeId),
                "un véhicule sans agence ne peut pas être désaffecté");
        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.desaffecterVehiculeDeAgence(-1L));
    }

    // ----------------------------- Employé -----------------------------

    @Test
    @DisplayName("Employé : affectation, transfert et désaffectation")
    void affecterTransfererDesaffecterEmploye() {
        Long employeId = creerEmploye();
        Long agenceA = creerAgence("Agence A");
        Long agenceB = creerAgence("Agence B");

        employeService.affecterEmployeAAgence(employeId, agenceA);
        assertEquals(agenceA, employeRepository.findById(employeId).orElseThrow().getAgence().getIdAgence());

        employeService.affecterEmployeAAgence(employeId, agenceB);
        assertEquals(agenceB, employeRepository.findById(employeId).orElseThrow().getAgence().getIdAgence());

        employeService.desaffecterEmployeDeAgence(employeId);
        assertNull(employeRepository.findById(employeId).orElseThrow().getAgence());

        assertThrows(IllegalArgumentException.class, () -> employeService.desaffecterEmployeDeAgence(employeId));
    }

    @Test
    @DisplayName("Employé : employé ou agence inexistant -> ResourceNotFoundException")
    void affecterEmployeInexistant() {
        Long employeId = creerEmploye();
        Long agenceId = creerAgence("Agence A");

        assertThrows(ResourceNotFoundException.class, () -> employeService.affecterEmployeAAgence(-1L, agenceId));
        assertThrows(ResourceNotFoundException.class, () -> employeService.affecterEmployeAAgence(employeId, -1L));
        assertThrows(ResourceNotFoundException.class, () -> employeService.desaffecterEmployeDeAgence(-1L));
    }
}
