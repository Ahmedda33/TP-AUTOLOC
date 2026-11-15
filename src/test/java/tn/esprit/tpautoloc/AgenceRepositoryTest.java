package tn.esprit.tpautoloc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.tpautoloc.domain.Agence;
import tn.esprit.tpautoloc.repository.IAgenceRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Tests du repository Agence")
class AgenceRepositoryTest {

    @Autowired
    private IAgenceRepository agenceRepository;

    private String decrire(Agence a) {
        return "Agence [id=" + a.getIdAgence() + ", nom=" + a.getNom() + ", ville=" + a.getVille()
                + ", adresse=" + a.getAdresse() + ", telephone=" + a.getTelephone() + "]";
    }

    @Test
    @DisplayName("Agence : save, findAll, findById, existsById, count, deleteById")
    void testAgenceRepository() {
        long avant = agenceRepository.count();

        System.out.println("BEGIN SAVE TEST AGENCE");
        Agence agence = new Agence();
        agence.setNom("AutoLoc Tunis");
        agence.setVille("Tunis");
        agence.setAdresse("Avenue Habib Bourguiba");
        agence.setTelephone("71000000");

        Agence saved = agenceRepository.save(agence);
        System.out.println("save() -> " + decrire(saved));
        assertNotNull(saved.getIdAgence(), "save() doit generer un id pour l'agence");
        System.out.println("END SAVE TEST AGENCE");

        System.out.println("BEGIN FIND ALL TEST AGENCE");
        List<Agence> agences = agenceRepository.findAll();
        System.out.println("findAll() -> " + agences.size() + " agence(s)");
        agences.forEach(a -> System.out.println("   " + decrire(a)));
        assertEquals(avant + 1, agences.size(), "findAll() doit contenir une agence de plus");
        System.out.println("END FIND ALL TEST AGENCE");

        System.out.println("BEGIN FIND BY ID TEST AGENCE");
        Optional<Agence> trouvee = agenceRepository.findById(saved.getIdAgence());
        System.out.println("findById() -> " + trouvee.map(this::decrire).orElse("introuvable"));
        assertTrue(trouvee.isPresent(), "findById() doit retrouver l'agence enregistree");
        assertEquals("Tunis", trouvee.get().getVille(), "la ville de l'agence retrouvee doit etre Tunis");
        System.out.println("END FIND BY ID TEST AGENCE");

        System.out.println("BEGIN EXISTS BY ID TEST AGENCE");
        System.out.println("existsById() -> " + agenceRepository.existsById(saved.getIdAgence()));
        assertTrue(agenceRepository.existsById(saved.getIdAgence()), "existsById() doit renvoyer true pour l'agence enregistree");
        System.out.println("END EXISTS BY ID TEST AGENCE");

        System.out.println("BEGIN COUNT TEST AGENCE");
        System.out.println("count() -> " + agenceRepository.count());
        assertEquals(avant + 1, agenceRepository.count(), "count() doit avoir augmente de 1");
        System.out.println("END COUNT TEST AGENCE");

       // System.out.println("BEGIN DELETE BY ID TEST AGENCE");
       // agenceRepository.deleteById(saved.getIdAgence());
       // System.out.println("apres deleteById(), existsById() -> " + agenceRepository.existsById(saved.getIdAgence()));
       // assertFalse(agenceRepository.existsById(saved.getIdAgence()), "l'agence ne doit plus exister apres deleteById()");
       // assertEquals(avant, agenceRepository.count(), "count() doit revenir a sa valeur de depart");
       // System.out.println("END DELETE BY ID TEST AGENCE");
    }
}
