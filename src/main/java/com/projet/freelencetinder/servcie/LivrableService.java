package com.projet.freelencetinder.servcie;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.projet.freelencetinder.dto.CreateLivrableRequest;
import com.projet.freelencetinder.dto.LivrableDto;
import com.projet.freelencetinder.models.StatusLivrable;

public interface LivrableService {

    /* Freelance => upload + création */
    LivrableDto uploadLivrable(CreateLivrableRequest req, Long freelancerId);

    /* Liste pour une mission (tri et filtrage statut) */
    List<LivrableDto> getLivrablesForMission(Long missionId,
                                             StatusLivrable status,
                                             Sort sort);

    /* Liste pour un freelance */
    List<LivrableDto> getLivrablesForFreelancer(Long freelancerId,
                                                Sort sort);

    /* Validation / rejet par le client */
    void validerLivrable(Long livrableId, Long clientId);
    void rejeterLivrable(Long livrableId, Long clientId, String raison);
}
