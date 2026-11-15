package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.web.dto.AgenceDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Atelier 4/7 : test d'intégration du service Agence (DTO). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Agence – tests d'intégration")
class AgenceServiceTest {

    @Autowired
    private IAgenceService agenceService;

    private AgenceDTO nouvelleAgence(String nom, String ville) {
        return AgenceDTO.builder().nom(nom).ville(ville).adresse("1 rue Test").telephone("71000000").build();
    }

    @Test
    @DisplayName("Agence : create, findAll, findById, update, count, deleteById")
    void testAgenceService() {
        AgenceDTO saved = agenceService.create(nouvelleAgence("Agence Tunis", "Tunis"));
        assertNotNull(saved.getId(), "create() doit generer un id pour l'agence");

        List<AgenceDTO> agences = agenceService.findAll();
        assertTrue(agences.stream().anyMatch(a -> a.getId().equals(saved.getId())),
                "findAll() doit contenir l'agence enregistree");

        AgenceDTO trouvee = agenceService.findById(saved.getId());
        assertEquals("Tunis", trouvee.getVille());

        AgenceDTO modif = nouvelleAgence("Agence Tunis Centre", "Tunis");
        AgenceDTO misAJour = agenceService.update(saved.getId(), modif);
        assertEquals(saved.getId(), misAJour.getId(), "update() ne doit pas changer l'identifiant");
        assertEquals("Agence Tunis Centre", agenceService.findById(saved.getId()).getNom());

        assertTrue(agenceService.count() > 0);
        assertTrue(agenceService.existsById(saved.getId()));

        agenceService.deleteById(saved.getId());
        assertFalse(agenceService.existsById(saved.getId()));
        assertThrows(ResourceNotFoundException.class, () -> agenceService.findById(saved.getId()));
    }

    @Test
    @DisplayName("Agence : regles de gestion (id fourni, nom obligatoire)")
    void testReglesAgence() {
        AgenceDTO avecId = nouvelleAgence("Avec id", "Sfax");
        avecId.setId(99L);
        assertThrows(IllegalArgumentException.class, () -> agenceService.create(avecId));

        AgenceDTO sansNom = nouvelleAgence(" ", "Sfax");
        assertThrows(IllegalArgumentException.class, () -> agenceService.create(sansNom));
    }

    @Test
    @DisplayName("Agence : identifiant inexistant -> ResourceNotFoundException")
    void testAgenceInexistante() {
        assertThrows(ResourceNotFoundException.class, () -> agenceService.findById(-1L));
        assertThrows(ResourceNotFoundException.class, () -> agenceService.deleteById(-1L));
        assertThrows(ResourceNotFoundException.class, () -> agenceService.update(-1L, nouvelleAgence("X", "Y")));
    }
}
