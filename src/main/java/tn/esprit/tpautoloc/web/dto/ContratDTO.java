package tn.esprit.tpautoloc.web.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Le champ {@code paiements} n'est pas dans l'énoncé : il est conservé pour que la création
 * d'un contrat avec ses paiements (cascade, Atelier 5) continue de fonctionner. Il n'est pas
 * validé en cascade car un paiement imbriqué n'a pas encore de contratId.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratDTO {
    private Long id;
    private LocalDate dateSignature;

    @PositiveOrZero(message = "Le montant total ne peut pas être négatif")
    private BigDecimal montantTotal;

    private Boolean valide;
    private Long reservationId;
    private List<PaiementDTO> paiements;
}
