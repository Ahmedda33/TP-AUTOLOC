package tn.esprit.tpautoloc.web.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import tn.esprit.tpautoloc.domain.Contrat;
import tn.esprit.tpautoloc.web.dto.ContratDTO;

@Mapper(componentModel = "spring", uses = PaiementMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContratMapper {

    @Mapping(target = "id", source = "idContrat")
    @Mapping(target = "reservationId", source = "reservation.idReservation")
    ContratDTO toDto(Contrat contrat);

    /** La réservation liée est chargée par le service à partir de reservationId. */
    @Mapping(target = "idContrat", source = "id")
    @Mapping(target = "reservation", ignore = true)
    Contrat toEntity(ContratDTO dto);

    /** Côté propriétaire de la composition : chaque paiement pointe vers son contrat. */
    @AfterMapping
    default void lierPaiements(@MappingTarget Contrat contrat) {
        if (contrat.getPaiements() != null) {
            contrat.getPaiements().forEach(paiement -> paiement.setContrat(contrat));
        }
    }
}
