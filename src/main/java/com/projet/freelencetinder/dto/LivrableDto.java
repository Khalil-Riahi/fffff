package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.projet.freelencetinder.models.StatusLivrable;

public class LivrableDto {

    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateEnvoi;
    private StatusLivrable status;
    private List<String> liensExternes;
    private List<String> cheminsFichiers;
    private Long missionId;
    private Long freelancerId;

    // ======== Getters / Setters ========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public StatusLivrable getStatus() { return status; }
    public void setStatus(StatusLivrable status) { this.status = status; }

    public List<String> getLiensExternes() { return liensExternes; }
    public void setLiensExternes(List<String> liensExternes) { this.liensExternes = liensExternes; }

    public List<String> getCheminsFichiers() { return cheminsFichiers; }
    public void setCheminsFichiers(List<String> cheminsFichiers) { this.cheminsFichiers = cheminsFichiers; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }
}
