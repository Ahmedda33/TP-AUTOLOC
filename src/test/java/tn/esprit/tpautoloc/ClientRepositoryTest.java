package tn.esprit.tpautoloc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.tpautoloc.domain.Client;
import tn.esprit.tpautoloc.repository.IClientRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Tests du repository Client")
class ClientRepositoryTest {

    @Autowired
    private IClientRepository clientRepository;

    private Client nouveauClient(String nom, String prenom, String email, String permis) {
        Client c = new Client();
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setEmail(email);
        c.setTelephone("20123456");
        c.setNumPermis(permis);
        c.setDateInscription(LocalDate.now());
        return c;
    }

    private String decrire(Client c) {
        return "Client [id=" + c.getIdClient() + ", nom=" + c.getNom() + ", prenom=" + c.getPrenom()
                + ", email=" + c.getEmail() + ", telephone=" + c.getTelephone()
                + ", numPermis=" + c.getNumPermis() + ", dateInscription=" + c.getDateInscription() + "]";
    }

    @Test
    @DisplayName("Client : save de deux clients, findAll, findById, existsById, count")
    void testClientRepository() {
        long avant = clientRepository.count();

        System.out.println("BEGIN SAVE TEST CLIENT");
        Client c1 = clientRepository.save(nouveauClient("Ben Salah", "Ali", "ali@mail.tn", "P-001"));
        Client c2 = clientRepository.save(nouveauClient("Trabelsi", "Sara", "sara@mail.tn", "P-002"));
        System.out.println("save() -> " + decrire(c1));
        System.out.println("save() -> " + decrire(c2));
        assertNotNull(c1.getIdClient(), "save() doit generer un id pour le premier client");
        assertNotNull(c2.getIdClient(), "save() doit generer un id pour le deuxieme client");
        assertNotEquals(c1.getIdClient(), c2.getIdClient(), "les deux clients doivent avoir des ids differents");
        System.out.println("END SAVE TEST CLIENT");

        System.out.println("BEGIN FIND ALL TEST CLIENT");
        List<Client> clients = clientRepository.findAll();
        System.out.println("findAll() -> " + clients.size() + " client(s)");
        clients.forEach(c -> System.out.println("   " + decrire(c)));
        assertEquals(avant + 2, clients.size(), "findAll() doit contenir deux clients de plus");
        System.out.println("END FIND ALL TEST CLIENT");

        System.out.println("BEGIN FIND BY ID TEST CLIENT");
        Client trouve = clientRepository.findById(c1.getIdClient()).orElse(null);
        assertNotNull(trouve, "findById() doit retrouver le premier client");
        System.out.println("findById() -> " + decrire(trouve));
        assertEquals("Ali", trouve.getPrenom(), "le prenom du client retrouve doit etre Ali");
        System.out.println("END FIND BY ID TEST CLIENT");

        System.out.println("BEGIN EXISTS BY ID TEST CLIENT");
        System.out.println("existsById(id existant) -> " + clientRepository.existsById(c2.getIdClient()));
        System.out.println("existsById(-1) -> " + clientRepository.existsById(-1L));
        assertTrue(clientRepository.existsById(c2.getIdClient()), "existsById() doit renvoyer true pour un id existant");
        assertFalse(clientRepository.existsById(-1L), "existsById() doit renvoyer false pour un id inexistant");
        System.out.println("END EXISTS BY ID TEST CLIENT");

        System.out.println("BEGIN COUNT TEST CLIENT");
        System.out.println("count() -> " + clientRepository.count());
        assertEquals(avant + 2, clientRepository.count(), "count() doit avoir augmente de 2");
        System.out.println("END COUNT TEST CLIENT");

        System.out.println("BEGIN DELETE BY ID TEST CLIENT");
        clientRepository.deleteById(c1.getIdClient());
        clientRepository.deleteById(c2.getIdClient());
        System.out.println("apres deleteById(), count() -> " + clientRepository.count());
        assertEquals(avant, clientRepository.count(), "count() doit revenir a sa valeur de depart");
        System.out.println("END DELETE BY ID TEST CLIENT");
    }
}
