package tn.esprit.tpautoloc.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import tn.esprit.tpautoloc.domain.Vehicule;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehiculeMapper {

    @Mapping(target = "id", source = "idVehicule")
    @Mapping(target = "agenceId", source = "agence.idAgence")
    VehiculeDTO toDto(Vehicule vehicule);

    /** L'agence est chargée par le service à partir de agenceId. */
    @Mapping(target = "idVehicule", source = "id")
    @Mapping(target = "agence", ignore = true)
    Vehicule toEntity(VehiculeDTO dto);
}
