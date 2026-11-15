package tn.esprit.tpautoloc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.tpautoloc.domain.Vehicule;
import tn.esprit.tpautoloc.domain.enums.CategorieVehicule;
import tn.esprit.tpautoloc.domain.enums.StatutVehicule;
import tn.esprit.tpautoloc.repository.IVehiculeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Tests du repository Vehicule")
class VehiculeRepositoryTest {

    @Autowired
    private IVehiculeRepository vehiculeRepository;

    private String decrire(Vehicule v) {
        return "Vehicule [id=" + v.getIdVehicule() + ", immatriculation=" + v.getImmatriculation()
                + ", marque=" + v.getMarque() + ", modele=" + v.getModele() + ", categorie=" + v.getCategorie()
                + ", tarifJournalier=" + v.getTarifJournalier() + ", statut=" + v.getStatut() + "]";
    }

    @Test
    @DisplayName("Vehicule : save, findAll, count, modification, deleteById")
    void testVehiculeRepository() {
        long avant = vehiculeRepository.count();

        Vehicule v = new Vehicule();
        v.setImmatriculation("999-TU-999");
        v.setMarque("Kia");
        v.setModele("Picanto");
        v.setCategorie(CategorieVehicule.CITADINE);
        v.setTarifJournalier(new BigDecimal("70.0"));
        v.setStatut(StatutVehicule.DISPONIBLE);

        System.out.println("BEGIN SAVE TEST VEHICULE");
        Vehicule saved = vehiculeRepository.save(v);
        System.out.println("save() -> " + decrire(saved));
        assertNotNull(saved.getIdVehicule(), "save() doit generer un id pour le vehicule");
        System.out.println("END SAVE TEST VEHICULE");

        System.out.println("BEGIN FIND ALL TEST VEHICULE");
        List<Vehicule> vehicules = vehiculeRepository.findAll();
        System.out.println("findAll() -> " + vehicules.size() + " vehicule(s)");
        vehicules.forEach(x -> System.out.println("   " + decrire(x)));
        assertEquals(avant + 1, vehicules.size(), "findAll() doit contenir un vehicule de plus");
        System.out.println("END FIND ALL TEST VEHICULE");

        System.out.println("BEGIN COUNT TEST VEHICULE");
        System.out.println("count() -> " + vehiculeRepository.count());
        assertEquals(avant + 1, vehiculeRepository.count(), "count() doit avoir augmente de 1");
        System.out.println("END COUNT TEST VEHICULE");

        System.out.println("BEGIN UPDATE TEST VEHICULE");
        saved.setStatut(StatutVehicule.LOUE);
        vehiculeRepository.save(saved);
        System.out.println("END UPDATE TEST VEHICULE");

        System.out.println("BEGIN FIND BY ID TEST VEHICULE");
        Vehicule modifie = vehiculeRepository.findById(saved.getIdVehicule()).orElseThrow();
        System.out.println("apres modification -> " + decrire(modifie));
        assertEquals(StatutVehicule.LOUE, modifie.getStatut(), "le statut doit etre LOUE apres la modification");
        System.out.println("END FIND BY ID TEST VEHICULE");

        System.out.println("BEGIN DELETE BY ID TEST VEHICULE");
        vehiculeRepository.deleteById(saved.getIdVehicule());
        System.out.println("apres deleteById(), existsById() -> " + vehiculeRepository.existsById(saved.getIdVehicule()));
        assertFalse(vehiculeRepository.existsById(saved.getIdVehicule()), "le vehicule ne doit plus exister apres deleteById()");
        assertEquals(avant, vehiculeRepository.count(), "count() doit revenir a sa valeur de depart");
        System.out.println("END DELETE BY ID TEST VEHICULE");
    }
}
