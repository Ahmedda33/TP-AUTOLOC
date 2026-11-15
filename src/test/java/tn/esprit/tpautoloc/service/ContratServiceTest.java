package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.domain.enums.ModePaiement;
import tn.esprit.tpautoloc.web.dto.ContratDTO;
import tn.esprit.tpautoloc.web.dto.PaiementDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Atelier 4/7 : test d'intégration du service Contrat (DTO) et de la cascade des paiements. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Contrat – tests d'intégration")
class ContratServiceTest {

    @Autowired
    private IContratService contratService;
    @Autowired
    private IPaiementService paiementService;

    private ContratDTO nouveauContrat(String montant, boolean valide) {
        return ContratDTO.builder().dateSignature(LocalDate.now())
                .montantTotal(new BigDecimal(montant)).valide(valide).build();
    }

    @Test
    @DisplayName("Contrat : create, findAll, findById, update, existsById, deleteById")
    void testContratService() {
        ContratDTO saved = contratService.create(nouveauContrat("500.00", true));
        assertNotNull(saved.getId());

        assertTrue(contratService.findAll().stream().anyMatch(c -> c.getId().equals(saved.getId())));

        ContratDTO trouve = contratService.findById(saved.getId());
        assertEquals(0, new BigDecimal("500.00").compareTo(trouve.getMontantTotal()));
        assertTrue(trouve.getValide());

        ContratDTO misAJour = contratService.update(saved.getId(), nouveauContrat("650.00", false));
        assertEquals(saved.getId(), misAJour.getId());
        assertEquals(0, new BigDecimal("650.00").compareTo(contratService.findById(saved.getId()).getMontantTotal()));

        assertTrue(contratService.existsById(saved.getId()));
        contratService.deleteById(saved.getId());
        assertFalse(contratService.existsById(saved.getId()));
        assertThrows(ResourceNotFoundException.class, () -> contratService.findById(saved.getId()));
    }

    @Test
    @DisplayName("Contrat : montant negatif et id fourni refuses")
    void testReglesContrat() {
        assertThrows(IllegalArgumentException.class, () -> contratService.create(nouveauContrat("-10.0", true)));
        ContratDTO avecId = nouveauContrat("10.0", true);
        avecId.setId(5L);
        assertThrows(IllegalArgumentException.class, () -> contratService.create(avecId));
    }

    @Test
    @DisplayName("Contrat : creation avec ses paiements (cascade) puis suppression en cascade")
    void testCascadePaiements() {
        ContratDTO dto = nouveauContrat("240.00", true);
        dto.setPaiements(List.of(
                PaiementDTO.builder().montant(new BigDecimal("100.00")).datePaiement(LocalDate.now())
                        .modePaiement(ModePaiement.CARTE).build(),
                PaiementDTO.builder().montant(new BigDecimal("140.00")).datePaiement(LocalDate.now())
                        .modePaiement(ModePaiement.CARTE).build()));

        ContratDTO saved = contratService.create(dto);
        assertEquals(2, saved.getPaiements().size());
        PaiementDTO premierPaiement = saved.getPaiements().get(0);
        assertNotNull(premierPaiement.getId());
        assertEquals(saved.getId(), premierPaiement.getContratId());
        assertTrue(paiementService.existsById(premierPaiement.getId()));

        // Les paiements restent lors d'une modification du contrat.
        contratService.update(saved.getId(), nouveauContrat("260.00", true));
        assertEquals(2, contratService.findById(saved.getId()).getPaiements().size());

        contratService.deleteById(saved.getId());
        assertFalse(paiementService.existsById(premierPaiement.getId()));
    }
}
