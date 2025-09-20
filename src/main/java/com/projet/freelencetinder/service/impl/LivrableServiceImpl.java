package com.projet.freelencetinder.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.projet.freelencetinder.dto.CreateLivrableRequest;
import com.projet.freelencetinder.dto.LivrableDto;
import com.projet.freelencetinder.exception.BusinessException; // ✅ pour 403
import com.projet.freelencetinder.models.Livrable;
import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.models.StatusLivrable;
import com.projet.freelencetinder.models.TranchePaiement;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.LivrableRepository;
import com.projet.freelencetinder.repository.MissionRepository;
import com.projet.freelencetinder.repository.TranchePaiementRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import com.projet.freelencetinder.servcie.EscrowService;
import com.projet.freelencetinder.servcie.FileStorageService;
import com.projet.freelencetinder.servcie.LivrableService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class LivrableServiceImpl implements LivrableService {

    private static final Logger logger = LoggerFactory.getLogger(LivrableServiceImpl.class);

    private final LivrableRepository livrableRepo;
    private final MissionRepository missionRepo;
    private final UtilisateurRepository userRepo;
    private final FileStorageService fileStorage;
    private final TranchePaiementRepository trancheRepo;
    private final EscrowService escrowService;

    @Value("${payment.mode:DIRECT_FLOUCI}")
    private String paymentMode;

    public LivrableServiceImpl(LivrableRepository livrableRepo,
                               MissionRepository missionRepo,
                               UtilisateurRepository userRepo,
                               FileStorageService fileStorage,
                               TranchePaiementRepository trancheRepo,
                               EscrowService escrowService) {
        this.livrableRepo = livrableRepo;
        this.missionRepo = missionRepo;
        this.userRepo = userRepo;
        this.fileStorage = fileStorage;
        this.trancheRepo = trancheRepo;
        this.escrowService = escrowService;
    }

    /* ========== Upload & création ========== */
    @Override
    public LivrableDto uploadLivrable(CreateLivrableRequest req, Long freelancerId) {
        Mission mission = missionRepo.findById(req.getMissionId())
            .orElseThrow(() -> new IllegalArgumentException("Mission introuvable"));

        Utilisateur freelance = userRepo.findById(freelancerId)
            .orElseThrow(() -> new IllegalArgumentException("Freelance introuvable"));

        if (!freelance.getId().equals(
                mission.getFreelanceSelectionne() != null
                    ? mission.getFreelanceSelectionne().getId()
                    : null)) {
            // 403
            throw new BusinessException("Accès refusé");
        }

        Livrable liv = new Livrable();
        liv.setTitre(req.getTitre());
        liv.setDescription(req.getDescription());
        liv.setMission(mission);
        liv.setFreelancer(freelance);

        List<String> chemins = new ArrayList<>();
        if (req.getFichiers() != null) {
            for (MultipartFile f : req.getFichiers()) {
                try {
                    String url = fileStorage.save(f);
                    chemins.add(url);
                } catch (IOException e) {
                    throw new RuntimeException("Erreur upload fichier : " + f.getOriginalFilename(), e);
                }
            }
        }
        liv.setCheminsFichiers(chemins);
        liv.setLiensExternes(req.getLiensExternes());

        livrableRepo.save(liv);
        return mapToDto(liv);
    }

    /* ========== Listing mission ========== */
    @Override
    @Transactional(readOnly = true)
    public List<LivrableDto> getLivrablesForMission(Long missionId, StatusLivrable status, Sort sort) {
        List<Livrable> list = (status == null)
            ? livrableRepo.findByMissionId(missionId, sort)
            : livrableRepo.findByMissionIdAndStatus(missionId, status, sort);

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /* ========== Listing freelance ========== */
    @Override
    @Transactional(readOnly = true)
    public List<LivrableDto> getLivrablesForFreelancer(Long freelancerId, Sort sort) {
        return livrableRepo.findByFreelancerId(freelancerId, sort)
                           .stream()
                           .map(this::mapToDto)
                           .collect(Collectors.toList());
    }

    /* ========== Validation / refus ========== */
    @Override
    public void validerLivrable(Long livrableId, Long clientId) {
        Livrable liv = getAndCheckClientRights(livrableId, clientId);
        liv.setStatus(StatusLivrable.VALIDE);
        livrableRepo.save(liv); // ✅ sauvegarde explicite, plus lisible

        // Logging avant paiement
        logger.info("Début paiement pour livrable - livrableId: {}, clientId: {}", livrableId, clientId);

        Long missionId = liv.getMission().getId();

        // La sélection de tranche est verrouillée pessimiste dans le repo
        if ("DIRECT_FLOUCI".equalsIgnoreCase(paymentMode)) {
            TranchePaiement tranche = trancheRepo
                .findNextForUpdate(missionId, StatutTranche.EN_ATTENTE_DEPOT)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Aucune tranche de paiement n'est définie. Créez un plan de paiement pour régler le freelance.")); // 422

            // ✅ anti double-liaison → 409
            logger.info("Tranche trouvée pour paiement direct - trancheId: {}, livrableId: {}", 
                    tranche.getId(), liv.getId());

            if (tranche.getLivrableAssocie() != null
                && !tranche.getLivrableAssocie().getId().equals(liv.getId())) {
                throw new IllegalStateException("Cette tranche est déjà liée à un autre livrable."); // 409
            }

            tranche.setLivrableAssocie(liv);
            trancheRepo.save(tranche);

            // Génère le lien et passe EN_ATTENTE_PAIEMENT
            escrowService.initPaiementDirect(tranche.getId(), clientId);

            logger.info("Paiement direct initié - trancheId: {}, clientId: {}", tranche.getId(), clientId);

        } else if ("ESCROW_PAYMEE".equalsIgnoreCase(paymentMode)) {
            TranchePaiement tranche = trancheRepo
                .findNextForUpdate(missionId, StatutTranche.FONDS_BLOQUES)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Aucune tranche avec fonds bloqués pour cette mission. Initiez un paiement (escrow) d'abord.")); // 422

            // ✅ anti double-liaison → 409
            logger.info("Tranche escrow trouvée - trancheId: {}, livrableId: {}", 
                    tranche.getId(), liv.getId());

            if (tranche.getLivrableAssocie() != null
                && !tranche.getLivrableAssocie().getId().equals(liv.getId())) {
                throw new IllegalStateException("Cette tranche est déjà liée à un autre livrable."); // 409
            }

            tranche.setLivrableAssocie(liv);
            trancheRepo.save(tranche);

            // Valide la tranche (capture après commit via event)
            escrowService.validerLivrable(tranche.getId(), clientId);

            logger.info("Validation escrow initiée - trancheId: {}, clientId: {}", tranche.getId(), clientId);
        } else {
            // 409 (conflit de configuration)
            throw new IllegalStateException("Mode de paiement inconnu: " + paymentMode);
        }

        // ✅ Recalculer le statut de la mission après validation du livrable
        // Cela permet de mettre à jour le statut mission (PRÊT_A_CLOTURER → TERMINEE)
        // en tenant compte du livrable maintenant validé (deliveryAccepted = true)
        escrowService.recomputeMissionStatus(missionId);
    }

    @Override
    public void rejeterLivrable(Long livrableId, Long clientId, String raison) {
        Livrable liv = getAndCheckClientRights(livrableId, clientId);
        liv.setStatus(StatusLivrable.REJETE);
        if (raison != null) {
            String desc = liv.getDescription() == null ? "" : liv.getDescription() + "\n";
            liv.setDescription(desc + "Motif rejet : " + raison);
        }
        livrableRepo.save(liv);
    }

    /* ========== Helpers ========== */
    private Livrable getAndCheckClientRights(Long livrableId, Long clientId) {
        Livrable liv = livrableRepo.findById(livrableId)
            .orElseThrow(() -> new IllegalArgumentException("Livrable inconnu")); // 422

        Long missionId = liv.getMission().getId();
        Long ownerIdInDb = liv.getMission().getClient().getId();

        // Logging structuré pour tracer les droits d'accès
        logger.info("Vérification droits client - livrableId: {}, missionId: {}, ownerIdInDb: {}, clientIdInput: {}",
                livrableId, missionId, ownerIdInDb, clientId);

        Long ownerId = liv.getMission().getClient().getId();
        if (!ownerId.equals(clientId)) {
            logger.warn("Accès refusé - mismatch clientId - livrableId: {}, ownerIdInDb: {}, clientIdInput: {}",
                    livrableId, ownerIdInDb, clientId);
            throw new BusinessException("Accès refusé");
        }
        return liv;
    }

    private LivrableDto mapToDto(Livrable l) {
        LivrableDto d = new LivrableDto();
        d.setId(l.getId());
        d.setTitre(l.getTitre());
        d.setDescription(l.getDescription());
        d.setDateEnvoi(l.getDateEnvoi());
        d.setStatus(l.getStatus());
        d.setLiensExternes(l.getLiensExternes());
        d.setCheminsFichiers(l.getCheminsFichiers());
        d.setMissionId(l.getMission().getId());
        d.setFreelancerId(l.getFreelancer().getId());
        return d;
    }
}
