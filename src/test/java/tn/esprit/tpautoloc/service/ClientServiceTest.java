package tn.esprit.tpautoloc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.web.dto.ClientDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Atelier 4/7 : test d'intégration du service Client (DTO). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("Service Client – tests d'intégration")
class ClientServiceTest {

    @Autowired
    private IClientService clientService;

    private ClientDTO nouveauClient(String nom, String prenom, String email, String numPermis) {
        return ClientDTO.builder().nom(nom).prenom(prenom).email(email).telephone("20111111")
                .numPermis(numPermis).dateInscription(LocalDate.now()).build();
    }

    @Test
    @DisplayName("Client : create (x2), findAll, findById, update, count, deleteById")
    void testClientService() {
        ClientDTO premier = clientService.create(nouveauClient("Ben Ali", "Sami", "sami.benali@autoloc.tn", "P-1001"));
        ClientDTO second = clientService.create(nouveauClient("Trabelsi", "Leila", "leila.trabelsi@autoloc.tn", "P-1002"));
        assertNotNull(premier.getId());
        assertNotNull(second.getId());
        assertNotEquals(premier.getId(), second.getId());

        var clients = clientService.findAll();
        assertTrue(clients.stream().anyMatch(c -> c.getId().equals(premier.getId())));
        assertTrue(clients.stream().anyMatch(c -> c.getId().equals(second.getId())));

        assertEquals("sami.benali@autoloc.tn", clientService.findById(premier.getId()).getEmail());

        ClientDTO modif = nouveauClient("Ben Ali", "Samir", "sami.benali@autoloc.tn", "P-1001");
        assertEquals("Samir", clientService.update(premier.getId(), modif).getPrenom());

        assertTrue(clientService.count() >= 2);

        clientService.deleteById(premier.getId());
        assertFalse(clientService.existsById(premier.getId()));
        assertThrows(ResourceNotFoundException.class, () -> clientService.findById(premier.getId()));
        clientService.deleteById(second.getId());
        assertFalse(clientService.existsById(second.getId()));
    }

    @Test
    @DisplayName("Client : regles de gestion (id fourni, nom et email obligatoires)")
    void testReglesClient() {
        ClientDTO avecId = nouveauClient("A", "B", "a@b.tn", "P-1");
        avecId.setId(99L);
        assertThrows(IllegalArgumentException.class, () -> clientService.create(avecId));
        assertThrows(IllegalArgumentException.class,
                () -> clientService.create(nouveauClient("A", "B", null, "P-1")));
        assertThrows(IllegalArgumentException.class,
                () -> clientService.create(nouveauClient(null, "B", "a@b.tn", "P-1")));
    }
}
