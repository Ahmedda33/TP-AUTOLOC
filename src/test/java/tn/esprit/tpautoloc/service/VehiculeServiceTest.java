package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;
import tn.esprit.tpautoloc.web.dto.AgenceDTO;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Atelier 4/7 : test d'intégration du service Vehicule (DTO). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Vehicule – tests d'intégration")
class VehiculeServiceTest {

    @Autowired
    private IVehiculeService vehiculeService;
    @Autowired
    private IAgenceService agenceService;

    private Long nouvelleAgence() {
        return agenceService.create(AgenceDTO.builder().nom("Agence Test").ville("Tunis").build()).getId();
    }

    private VehiculeDTO nouveauVehicule(String tarif, Long agenceId) {
        return VehiculeDTO.builder().immatriculation("999-TU-999").marque("Kia").modele("Picanto")
                .categorie(CategorieVehicule.CITADINE).tarifJournalier(new BigDecimal(tarif))
                .statut(StatutVehicule.DISPONIBLE).agenceId(agenceId).build();
    }

    @Test
    @DisplayName("Vehicule : create, findAll, findById, update, count, deleteById")
    void testVehiculeService() {
        Long agenceId = nouvelleAgence();
        VehiculeDTO saved = vehiculeService.create(nouveauVehicule("70.0", agenceId));
        assertNotNull(saved.getId());
        assertEquals(agenceId, saved.getAgenceId());

        assertTrue(vehiculeService.findAll().stream().anyMatch(v -> v.getId().equals(saved.getId())));
        assertEquals("Picanto", vehiculeService.findById(saved.getId()).getModele());

        VehiculeDTO modif = nouveauVehicule("75.0", agenceId);
        modif.setModele("Rio");
        VehiculeDTO misAJour = vehiculeService.update(saved.getId(), modif);
        assertEquals(saved.getId(), misAJour.getId());
        assertEquals("Rio", vehiculeService.findById(saved.getId()).getModele());

        assertTrue(vehiculeService.count() > 0);

        vehiculeService.deleteById(saved.getId());
        assertFalse(vehiculeService.existsById(saved.getId()));
        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.findById(saved.getId()));
    }

    @Test
    @DisplayName("Vehicule : tarif negatif, id fourni et agence inexistante refuses")
    void testReglesVehicule() {
        Long agenceId = nouvelleAgence();
        assertThrows(IllegalArgumentException.class, () -> vehiculeService.create(nouveauVehicule("-5.0", agenceId)));

        VehiculeDTO avecId = nouveauVehicule("50.0", agenceId);
        avecId.setId(7L);
        assertThrows(IllegalArgumentException.class, () -> vehiculeService.create(avecId));

        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.create(nouveauVehicule("50.0", -1L)));
    }
}
