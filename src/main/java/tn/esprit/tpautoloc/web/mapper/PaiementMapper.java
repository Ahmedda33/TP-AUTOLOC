package tn.esprit.tpautoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import tn.esprit.tpautoloc.domain.Paiement;
import tn.esprit.tpautoloc.web.dto.PaiementDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaiementMapper {

    @Mapping(target = "id", source = "idPaiement")
    @Mapping(target = "contratId", source = "contrat.idContrat")
    PaiementDTO toDto(Paiement paiement);

    /** Le contrat parent est renseigné par ContratMapper ou par le service. */
    @Mapping(target = "idPaiement", source = "id")
    @Mapping(target = "contrat", ignore = true)
    Paiement toEntity(PaiementDTO dto);
}
