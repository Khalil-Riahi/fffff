package com.projet.freelencetinder.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.projet.freelencetinder.dto.paiement.TranchePaiementCreateDTO;
import com.projet.freelencetinder.dto.paiement.TranchePaiementResponseDTO;
import com.projet.freelencetinder.models.TranchePaiement;

@Mapper(componentModel = "spring")
public interface TranchePaiementMapper {

    TranchePaiement toEntity(TranchePaiementCreateDTO dto);

    TranchePaiementResponseDTO toDto(TranchePaiement entity);
}
