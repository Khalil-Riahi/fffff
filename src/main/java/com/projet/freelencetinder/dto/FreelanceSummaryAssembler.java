package com.projet.freelencetinder.dto;

import java.util.*;
import java.util.stream.Collectors;

import com.projet.freelencetinder.models.Utilisateur;

public final class FreelanceSummaryAssembler {

    private FreelanceSummaryAssembler() {}

    public static FreelanceSummaryDTO toSummary(Utilisateur u) {
        FreelanceSummaryDTO dto = new FreelanceSummaryDTO();

        dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setPrenom(u.getPrenom());
        dto.setPhotoUrl(u.getPhotoProfilUrl());
        dto.setLocalisation(u.getLocalisation());
        dto.setGouvernorat(u.getGouvernorat());

        dto.setNiveauExperience(u.getNiveauExperience() != null ? u.getNiveauExperience().name() : null);
        dto.setDisponibilite(u.getDisponibilite() != null ? u.getDisponibilite().name() : null);

        dto.setTarifHoraire(u.getTarifHoraire());
        dto.setTarifJournalier(u.getTarifJournalier());
        dto.setNoteMoyenne(u.getNoteMoyenne());

        dto.setCompetences(u.getCompetences() != null ? new HashSet<>(u.getCompetences()) : null);

        // Badge principal = premier badge s'il existe
        if (u.getListeBadges() != null && !u.getListeBadges().isEmpty()) {
            dto.setBadgePrincipal(u.getListeBadges().iterator().next());
        }

        dto.setTitreProfil(u.getTitreProfil());
        dto.setAnneesExperience(u.getAnneesExperience());
        dto.setMobilite(u.getMobilite());
        dto.setTimezone(u.getTimezone());

        dto.setModelesEngagementPreferes(
                u.getModelesEngagementPreferes() != null
                        ? new ArrayList<>(u.getModelesEngagementPreferes())
                        : null
        );

        dto.setDateDisponibilite(u.getDateDisponibilite() != null ? u.getDateDisponibilite().toString() : null);
        dto.setChargeHebdoSouhaiteeJours(u.getChargeHebdoSouhaiteeJours());

        dto.setLangues(u.getLangues() != null ? new HashMap<>(u.getLangues()) : null);
        dto.setCompetencesNiveaux(u.getCompetencesNiveaux() != null ? new HashMap<>(u.getCompetencesNiveaux()) : null);

        dto.setTauxReussite(u.getTauxReussite());
        dto.setTauxRespectDelais(u.getTauxRespectDelais());
        dto.setTauxReembauche(u.getTauxReembauche());
        dto.setDelaiReponseHeures(u.getDelaiReponseHeures());
        dto.setDelaiReponseMedianMinutes(u.getDelaiReponseMedianMinutes());

        dto.setCertifications(u.getCertifications() != null ? new ArrayList<>(u.getCertifications()) : null);
        dto.setPortfolioUrls(u.getPortfolioUrls() != null ? new ArrayList<>(u.getPortfolioUrls()) : null);

        // Vérifications & KYC
        dto.setEmailVerifie(u.isEmailVerifie());
        dto.setTelephoneVerifie(u.isTelephoneVerifie());
        dto.setIdentiteVerifiee(u.isIdentiteVerifiee());
        dto.setRibVerifie(u.isRibVerifie());
        dto.setKycStatut(u.getKycStatut());

        // Matching (pas calculé ici)
        dto.setMatchScore(null);
        dto.setMatchReasons(null);

        // Nouveaux champs demandés
        dto.setPreferenceDuree(u.getPreferenceDuree());
        dto.setNombreAvis(u.getNombreAvis());
        dto.setLinkedinUrl(u.getLinkedinUrl());
        dto.setGithubUrl(u.getGithubUrl());

        return dto;
    }
}
