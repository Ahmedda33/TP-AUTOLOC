package tn.esprit.tpautoloc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.tpautoloc.domain.*;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.RoleEmploye;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;
import tn.esprit.tpautoloc.repository.IEmployeRepository;
import tn.esprit.tpautoloc.repository.IEquipementRepository;
import tn.esprit.tpautoloc.repository.IMaintenanceRepository;
import tn.esprit.tpautoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/** Partie 5 : exercice autonome (Employe, Equipement, Maintenance) + table d'association. */
@SpringBootTest
@DisplayName("Exercice autonome : Employe, Equipement, Maintenance")
class AutresRepositoriesTest {

    @Autowired
    private IEmployeRepository employeRepository;
    @Autowired
    private IEquipementRepository equipementRepository;
    @Autowired
    private IMaintenanceRepository maintenanceRepository;
    @Autowired
    private IVehiculeRepository vehiculeRepository;

    @Test
    @DisplayName("Employe : save, findAll, existsById, deleteById")
    void testEmployeRepository() {
        Employe e = new Employe();
        e.setNom("Gharbi");
        e.setPrenom("Mehdi");
        e.setRole(RoleEmploye.AGENT);

        System.out.println("BEGIN SAVE TEST EMPLOYE");
        Employe saved = employeRepository.save(e);
        System.out.println("save() -> Employe [id=" + saved.getIdEmploye() + ", nom=" + saved.getNom() + ", prenom=" + saved.getPrenom() + ", role=" + saved.getRole() + "]");
        assertNotNull(saved.getIdEmploye(), "save() doit generer un id pour l'employe");
        System.out.println("END SAVE TEST EMPLOYE");

        System.out.println("BEGIN FIND ALL TEST EMPLOYE");
        System.out.println("findAll() -> " + employeRepository.findAll().size() + " employe(s)");
        assertTrue(employeRepository.findAll().stream().anyMatch(x -> x.getIdEmploye().equals(saved.getIdEmploye())), "findAll() doit contenir l'employe enregistre");
        System.out.println("END FIND ALL TEST EMPLOYE");

        System.out.println("BEGIN EXISTS BY ID TEST EMPLOYE");
        System.out.println("existsById() -> " + employeRepository.existsById(saved.getIdEmploye()));
        assertTrue(employeRepository.existsById(saved.getIdEmploye()), "existsById() doit renvoyer true pour l'employe enregistre");
        System.out.println("END EXISTS BY ID TEST EMPLOYE");

        System.out.println("BEGIN DELETE BY ID TEST EMPLOYE");
        employeRepository.deleteById(saved.getIdEmploye());
        System.out.println("apres deleteById(), existsById() -> " + employeRepository.existsById(saved.getIdEmploye()));
        assertFalse(employeRepository.existsById(saved.getIdEmploye()), "l'employe ne doit plus exister apres deleteById()");
        System.out.println("END DELETE BY ID TEST EMPLOYE");
    }

    @Test
    @DisplayName("Equipement : save, findById, existsById, deleteById")
    void testEquipementRepository() {
        Equipement eq = new Equipement();
        eq.setLibelle("GPS");

        System.out.println("BEGIN SAVE TEST EQUIPEMENT");
        Equipement saved = equipementRepository.save(eq);
        System.out.println("save() -> Equipement [id=" + saved.getIdEquipement() + ", libelle=" + saved.getLibelle() + "]");
        assertNotNull(saved.getIdEquipement(), "save() doit generer un id pour l'equipement");
        System.out.println("END SAVE TEST EQUIPEMENT");

        System.out.println("BEGIN FIND BY ID TEST EQUIPEMENT");
        System.out.println("findById() -> " + equipementRepository.findById(saved.getIdEquipement()).isPresent());
        assertEquals("GPS", equipementRepository.findById(saved.getIdEquipement()).orElseThrow().getLibelle(), "le libelle de l'equipement retrouve doit etre GPS");
        System.out.println("END FIND BY ID TEST EQUIPEMENT");

        System.out.println("BEGIN EXISTS BY ID TEST EQUIPEMENT");
        System.out.println("existsById() -> " + equipementRepository.existsById(saved.getIdEquipement()));
        assertTrue(equipementRepository.existsById(saved.getIdEquipement()), "existsById() doit renvoyer true pour l'equipement enregistre");
        System.out.println("END EXISTS BY ID TEST EQUIPEMENT");

        System.out.println("BEGIN DELETE BY ID TEST EQUIPEMENT");
        equipementRepository.deleteById(saved.getIdEquipement());
        System.out.println("apres deleteById(), existsById() -> " + equipementRepository.existsById(saved.getIdEquipement()));
        assertFalse(equipementRepository.existsById(saved.getIdEquipement()), "l'equipement ne doit plus exister apres deleteById()");
        System.out.println("END DELETE BY ID TEST EQUIPEMENT");
    }

    @Test
    @DisplayName("Maintenance : save, findById, existsById, deleteById")
    void testMaintenanceRepository() {
        Maintenance m = new Maintenance();
        m.setDateDebut(LocalDate.now());
        m.setDateFin(LocalDate.now().plusDays(2));
        m.setDescription("Vidange");

        System.out.println("BEGIN SAVE TEST MAINTENANCE");
        Maintenance saved = maintenanceRepository.save(m);
        System.out.println("save() -> Maintenance [id=" + saved.getIdMaintenance() + ", dateDebut=" + saved.getDateDebut() + ", dateFin=" + saved.getDateFin() + ", description=" + saved.getDescription() + "]");
        assertNotNull(saved.getIdMaintenance(), "save() doit generer un id pour la maintenance");
        System.out.println("END SAVE TEST MAINTENANCE");

        System.out.println("BEGIN FIND BY ID TEST MAINTENANCE");
        System.out.println("findById() -> " + maintenanceRepository.findById(saved.getIdMaintenance()).isPresent());
        assertTrue(maintenanceRepository.findById(saved.getIdMaintenance()).isPresent(), "findById() doit retrouver la maintenance enregistree");
        System.out.println("END FIND BY ID TEST MAINTENANCE");

        System.out.println("BEGIN EXISTS BY ID TEST MAINTENANCE");
        System.out.println("existsById() -> " + maintenanceRepository.existsById(saved.getIdMaintenance()));
        assertTrue(maintenanceRepository.existsById(saved.getIdMaintenance()), "existsById() doit renvoyer true pour la maintenance enregistree");
        System.out.println("END EXISTS BY ID TEST MAINTENANCE");

        System.out.println("BEGIN DELETE BY ID TEST MAINTENANCE");
        maintenanceRepository.deleteById(saved.getIdMaintenance());
        System.out.println("apres deleteById(), existsById() -> " + maintenanceRepository.existsById(saved.getIdMaintenance()));
        assertFalse(maintenanceRepository.existsById(saved.getIdMaintenance()), "la maintenance ne doit plus exister apres deleteById()");
        System.out.println("END DELETE BY ID TEST MAINTENANCE");
    }

    @Test
    @DisplayName("Table d'association Vehicule - Equipement")
    void testTableAssociationVehiculeEquipement() {
        Equipement gps = equipementRepository.save(libelle("GPS-assoc"));

        Vehicule v = new Vehicule();
        v.setImmatriculation("555-TU-555");
        v.setMarque("Fiat");
        v.setModele("500");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setTarifJournalier(new BigDecimal("60.0"));
        v.setStatut(StatutVehicule.DISPONIBLE);
        v.setVehiculeEquipements(new ArrayList<>());
        v.getVehiculeEquipements().add(new VehiculeEquipement(v, gps));

        System.out.println("BEGIN SAVE TEST ASSOCIATION VEHICULE EQUIPEMENT");
        Vehicule saved = vehiculeRepository.save(v);
        assertNotNull(saved.getIdVehicule(), "save() doit generer un id pour le vehicule");

        VehiculeEquipement ligne = saved.getVehiculeEquipements().get(0);
        assertEquals(saved.getIdVehicule(), ligne.getId().getIdVehicule(), "la ligne d'association doit porter l'id du vehicule");
        System.out.println("ligne d'association -> [idVehicule=" + ligne.getId().getIdVehicule() + ", idEquipement=" + ligne.getId().getIdEquipement() + "]");
        assertEquals(gps.getIdEquipement(), ligne.getId().getIdEquipement(), "la ligne d'association doit porter l'id de l'equipement");
        System.out.println("END SAVE TEST ASSOCIATION VEHICULE EQUIPEMENT");

        System.out.println("BEGIN DELETE BY ID TEST ASSOCIATION VEHICULE EQUIPEMENT");
        vehiculeRepository.deleteById(saved.getIdVehicule());
        equipementRepository.deleteById(gps.getIdEquipement());
        System.out.println("apres deleteById(), vehicule existe -> " + vehiculeRepository.existsById(saved.getIdVehicule()));
        assertFalse(equipementRepository.existsById(gps.getIdEquipement()), "l'equipement ne doit plus exister apres deleteById()");
        System.out.println("END DELETE BY ID TEST ASSOCIATION VEHICULE EQUIPEMENT");
    }

    private Equipement libelle(String l) {
        Equipement e = new Equipement();
        e.setLibelle(l);
        return e;
    }
}
