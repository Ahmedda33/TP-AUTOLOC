package tn.esprit.tpautoloc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.tpautoloc.domain.Employe;
import tn.esprit.tpautoloc.domain.Equipement;
import tn.esprit.tpautoloc.domain.enums.RoleEmploye;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.service.IContratService;
import tn.esprit.tpautoloc.service.IEmployeService;
import tn.esprit.tpautoloc.service.IEquipementService;
import tn.esprit.tpautoloc.web.dto.ContratDTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Exercice autonome : services Employe, Equipement, Contrat")
class AutresServicesTest {

    @Autowired
    private IEmployeService employeService;
    @Autowired
    private IEquipementService equipementService;
    @Autowired
    private IContratService contratService;

    @Test
    @DisplayName("Employe : create, findAll, findById, existsById, deleteById")
    void testEmployeService() {
        Employe e = new Employe();
        e.setNom("Gharbi");
        e.setPrenom("Mehdi");
        e.setRole(RoleEmploye.AGENT);

        System.out.println("BEGIN CREATE TEST EMPLOYE SERVICE");
        Employe saved = employeService.create(e);
        System.out.println("create() -> Employe [id=" + saved.getIdEmploye() + ", nom=" + saved.getNom()
                + ", prenom=" + saved.getPrenom() + ", role=" + saved.getRole() + "]");
        assertNotNull(saved.getIdEmploye(), "create() doit generer un id pour l'employe");
        System.out.println("END CREATE TEST EMPLOYE SERVICE");

        System.out.println("BEGIN FIND ALL TEST EMPLOYE SERVICE");
        System.out.println("findAll() -> " + employeService.findAll().size() + " employe(s)");
        assertTrue(employeService.findAll().stream().anyMatch(x -> x.getIdEmploye().equals(saved.getIdEmploye())),
                "findAll() doit contenir l'employe enregistre");
        System.out.println("END FIND ALL TEST EMPLOYE SERVICE");

        System.out.println("BEGIN FIND BY ID TEST EMPLOYE SERVICE");
        Employe trouve = employeService.findById(saved.getIdEmploye());
        System.out.println("findById() -> Employe [id=" + trouve.getIdEmploye() + ", nom=" + trouve.getNom()
                + ", prenom=" + trouve.getPrenom() + ", role=" + trouve.getRole() + "]");
        assertEquals("Gharbi", trouve.getNom(), "le nom de l'employe retrouve doit etre Gharbi");
        System.out.println("END FIND BY ID TEST EMPLOYE SERVICE");

        System.out.println("BEGIN EXISTS BY ID TEST EMPLOYE SERVICE");
        System.out.println("existsById() -> " + employeService.existsById(saved.getIdEmploye()));
        assertTrue(employeService.existsById(saved.getIdEmploye()), "existsById() doit renvoyer true pour l'employe enregistre");
        System.out.println("END EXISTS BY ID TEST EMPLOYE SERVICE");

        System.out.println("BEGIN RULE TEST EMPLOYE SERVICE");
        Employe sansRole = new Employe();
        sansRole.setNom("Sans");
        assertThrows(IllegalArgumentException.class, () -> employeService.create(sansRole),
                "create() doit refuser un employe sans role");
        System.out.println("create() refuse un employe sans role");
        System.out.println("END RULE TEST EMPLOYE SERVICE");

        System.out.println("BEGIN DELETE BY ID TEST EMPLOYE SERVICE");
        employeService.deleteById(saved.getIdEmploye());
        System.out.println("apres deleteById(), existsById() -> " + employeService.existsById(saved.getIdEmploye()));
        assertFalse(employeService.existsById(saved.getIdEmploye()), "l'employe ne doit plus exister apres deleteById()");
        assertThrows(ResourceNotFoundException.class, () -> employeService.findById(saved.getIdEmploye()),
                "findById() doit lever ResourceNotFoundException apres la suppression");
        System.out.println("END DELETE BY ID TEST EMPLOYE SERVICE");
    }

    @Test
    @DisplayName("Equipement : create, findById, existsById, deleteById")
    void testEquipementService() {
        Equipement eq = new Equipement();
        eq.setLibelle("GPS");

        System.out.println("BEGIN CREATE TEST EQUIPEMENT SERVICE");
        Equipement saved = equipementService.create(eq);
        System.out.println("create() -> Equipement [id=" + saved.getIdEquipement() + ", libelle=" + saved.getLibelle() + "]");
        assertNotNull(saved.getIdEquipement(), "create() doit generer un id pour l'equipement");
        System.out.println("END CREATE TEST EQUIPEMENT SERVICE");

        System.out.println("BEGIN FIND BY ID TEST EQUIPEMENT SERVICE");
        Equipement trouve = equipementService.findById(saved.getIdEquipement());
        System.out.println("findById() -> Equipement [id=" + trouve.getIdEquipement() + ", libelle=" + trouve.getLibelle() + "]");
        assertEquals("GPS", trouve.getLibelle(), "le libelle de l'equipement retrouve doit etre GPS");
        System.out.println("END FIND BY ID TEST EQUIPEMENT SERVICE");

        System.out.println("BEGIN EXISTS BY ID TEST EQUIPEMENT SERVICE");
        System.out.println("existsById() -> " + equipementService.existsById(saved.getIdEquipement()));
        assertTrue(equipementService.existsById(saved.getIdEquipement()), "existsById() doit renvoyer true pour l'equipement enregistre");
        System.out.println("END EXISTS BY ID TEST EQUIPEMENT SERVICE");

        System.out.println("BEGIN RULE TEST EQUIPEMENT SERVICE");
        Equipement sansLibelle = new Equipement();
        assertThrows(IllegalArgumentException.class, () -> equipementService.create(sansLibelle),
                "create() doit refuser un equipement sans libelle");
        System.out.println("create() refuse un equipement sans libelle");
        System.out.println("END RULE TEST EQUIPEMENT SERVICE");

        System.out.println("BEGIN DELETE BY ID TEST EQUIPEMENT SERVICE");
        equipementService.deleteById(saved.getIdEquipement());
        System.out.println("apres deleteById(), existsById() -> " + equipementService.existsById(saved.getIdEquipement()));
        assertFalse(equipementService.existsById(saved.getIdEquipement()), "l'equipement ne doit plus exister apres deleteById()");
        assertThrows(ResourceNotFoundException.class, () -> equipementService.findById(saved.getIdEquipement()),
                "findById() doit lever ResourceNotFoundException apres la suppression");
        System.out.println("END DELETE BY ID TEST EQUIPEMENT SERVICE");
    }

    @Test
    @DisplayName("Contrat : create, findAll, findById, update, existsById, deleteById")
    void testContratService() {
        ContratDTO c = ContratDTO.builder().dateSignature(LocalDate.now())
                .montantTotal(new BigDecimal("500.0")).valide(true).build();

        System.out.println("BEGIN CREATE TEST CONTRAT SERVICE");
        ContratDTO saved = contratService.create(c);
        System.out.println("create() -> Contrat [id=" + saved.getId() + ", dateSignature=" + saved.getDateSignature()
                + ", montantTotal=" + saved.getMontantTotal() + ", valide=" + saved.getValide() + "]");
        assertNotNull(saved.getId(), "create() doit generer un id pour le contrat");
        System.out.println("END CREATE TEST CONTRAT SERVICE");

        System.out.println("BEGIN FIND ALL TEST CONTRAT SERVICE");
        System.out.println("findAll() -> " + contratService.findAll().size() + " contrat(s)");
        assertTrue(contratService.findAll().stream().anyMatch(x -> x.getId().equals(saved.getId())),
                "findAll() doit contenir le contrat enregistre");
        System.out.println("END FIND ALL TEST CONTRAT SERVICE");

        System.out.println("BEGIN FIND BY ID TEST CONTRAT SERVICE");
        ContratDTO trouve = contratService.findById(saved.getId());
        assertEquals(0, new BigDecimal("500.0").compareTo(trouve.getMontantTotal()), "le montant du contrat retrouve doit etre 500");
        System.out.println("END FIND BY ID TEST CONTRAT SERVICE");

        System.out.println("BEGIN UPDATE TEST CONTRAT SERVICE");
        ContratDTO modif = ContratDTO.builder().dateSignature(LocalDate.now())
                .montantTotal(new BigDecimal("650.0")).valide(false).build();
        ContratDTO misAJour = contratService.update(saved.getId(), modif);
        assertEquals(saved.getId(), misAJour.getId(), "update() ne doit pas changer l'identifiant");
        assertEquals(0, new BigDecimal("650.0").compareTo(contratService.findById(saved.getId()).getMontantTotal()),
                "le montant du contrat doit etre mis a jour en base");
        System.out.println("END UPDATE TEST CONTRAT SERVICE");

        System.out.println("BEGIN EXISTS BY ID TEST CONTRAT SERVICE");
        assertTrue(contratService.existsById(saved.getId()), "existsById() doit renvoyer true pour le contrat enregistre");
        System.out.println("END EXISTS BY ID TEST CONTRAT SERVICE");

        System.out.println("BEGIN RULE TEST CONTRAT SERVICE");
        ContratDTO negatif = ContratDTO.builder().montantTotal(new BigDecimal("-10.0")).build();
        assertThrows(IllegalArgumentException.class, () -> contratService.create(negatif),
                "create() doit refuser un montant total negatif");
        System.out.println("END RULE TEST CONTRAT SERVICE");

        System.out.println("BEGIN DELETE BY ID TEST CONTRAT SERVICE");
        contratService.deleteById(saved.getId());
        assertFalse(contratService.existsById(saved.getId()), "le contrat ne doit plus exister apres deleteById()");
        assertThrows(ResourceNotFoundException.class, () -> contratService.findById(saved.getId()),
                "findById() doit lever ResourceNotFoundException apres la suppression");
        System.out.println("END DELETE BY ID TEST CONTRAT SERVICE");
    }
}
