package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.projet.freelencetinder.models.StatusLivrable;

/**
 * DTO léger pour les livrables dans la vue détail mission.
 * Contient les informations essentielles avec permissions d'édition.
 */
public class LivrableLiteDTO {

    private Long id;
    private String titre;
    private String description;
    private StatusLivrable status;
    private LocalDateTime dateEnvoi;
    private List<String> cheminsFichiers;
    private List<String> liensExternes;
    private boolean canValidate; // true si le viewer est le client et status EN_ATTENTE
    private boolean canReject;   // true si le viewer est le client et status EN_ATTENTE

    /* ===================== Getters & Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public StatusLivrable getStatus() { return status; }
    public void setStatus(StatusLivrable status) { this.status = status; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public List<String> getCheminsFichiers() { return cheminsFichiers; }
    public void setCheminsFichiers(List<String> cheminsFichiers) { this.cheminsFichiers = cheminsFichiers; }

    public List<String> getLiensExternes() { return liensExternes; }
    public void setLiensExternes(List<String> liensExternes) { this.liensExternes = liensExternes; }

    public boolean isCanValidate() { return canValidate; }
    public void setCanValidate(boolean canValidate) { this.canValidate = canValidate; }

    public boolean isCanReject() { return canReject; }
    public void setCanReject(boolean canReject) { this.canReject = canReject; }
}