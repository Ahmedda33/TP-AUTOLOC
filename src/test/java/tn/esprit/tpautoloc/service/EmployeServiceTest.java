package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.domain.Employe;
import tn.esprit.tpautoloc.domain.enums.RoleEmploye;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Atelier 4 – Partie 2 (exercice autonome) : test d'intégration Spring du service Employe.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Employe – tests d'intégration")
class EmployeServiceTest {

    @Autowired
    private IEmployeService employeService;

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    private String decrire(Employe e) {
        return "Employe [id=" + e.getIdEmploye()
                + ", nom=" + e.getNom()
                + ", prenom=" + e.getPrenom()
                + ", role=" + e.getRole() + "]";
    }

    private Employe nouveauEmploye(String nom, String prenom, RoleEmploye role) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setRole(role);
        return e;
    }

    // -----------------------------------------------------------------------
    // Test principal : create → findAll → findById → count → deleteById
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Employe : create, findAll, findById, count, deleteById")
    void testEmployeService() {

        // ── CREATE ──────────────────────────────────────────────────────────
        System.out.println("BEGIN CREATE TEST EMPLOYE SERVICE");
        Employe saved = employeService.create(nouveauEmploye("Gharbi", "Mehdi", RoleEmploye.AGENT));
        System.out.println("create() -> " + decrire(saved));
        assertNotNull(saved.getIdEmploye(), "create() doit generer un id pour l'employe");
        System.out.println("END CREATE TEST EMPLOYE SERVICE");

        // ── FIND ALL ─────────────────────────────────────────────────────────
        System.out.println("BEGIN FIND ALL TEST EMPLOYE SERVICE");
        List<Employe> employes = employeService.findAll();
        System.out.println("findAll() -> " + employes.size() + " employe(s)");
        employes.forEach(e -> System.out.println("   " + decrire(e)));
        assertFalse(employes.isEmpty(), "findAll() ne doit pas renvoyer une liste vide");
        assertTrue(employes.stream().anyMatch(e -> e.getIdEmploye().equals(saved.getIdEmploye())),
                "findAll() doit contenir l'employe enregistre");
        System.out.println("END FIND ALL TEST EMPLOYE SERVICE");

        // ── FIND BY ID ───────────────────────────────────────────────────────
        System.out.println("BEGIN FIND BY ID TEST EMPLOYE SERVICE");
        Employe trouve = employeService.findById(saved.getIdEmploye());
        System.out.println("findById() -> " + decrire(trouve));
        assertNotNull(trouve, "findById() doit retrouver l'employe enregistre");
        assertEquals("Gharbi", trouve.getNom(), "le nom de l'employe retrouve doit etre Gharbi");
        assertEquals(RoleEmploye.AGENT, trouve.getRole(),
                "le role de l'employe retrouve doit etre AGENT");
        System.out.println("END FIND BY ID TEST EMPLOYE SERVICE");

        // ── COUNT ────────────────────────────────────────────────────────────
        System.out.println("BEGIN COUNT TEST EMPLOYE SERVICE");
        long count = employeService.count();
        System.out.println("count() -> " + count);
        assertTrue(count > 0, "count() doit etre superieur a 0 apres creation");
        System.out.println("END COUNT TEST EMPLOYE SERVICE");

        // ── DELETE BY ID ─────────────────────────────────────────────────────
        System.out.println("BEGIN DELETE BY ID TEST EMPLOYE SERVICE");
        employeService.deleteById(saved.getIdEmploye());
        System.out.println("apres deleteById(), existsById() -> "
                + employeService.existsById(saved.getIdEmploye()));
        assertFalse(employeService.existsById(saved.getIdEmploye()),
                "l'employe ne doit plus exister apres deleteById()");
        assertThrows(ResourceNotFoundException.class,
                () -> employeService.findById(saved.getIdEmploye()),
                "findById() doit lever ResourceNotFoundException apres suppression");
        System.out.println("END DELETE BY ID TEST EMPLOYE SERVICE");
    }
}
