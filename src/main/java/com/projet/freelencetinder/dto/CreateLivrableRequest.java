package com.projet.freelencetinder.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

public class CreateLivrableRequest {

    @NotBlank @Size(max = 180)
    private String titre;

    @Size(max = 4000)
    private String description;

    /**
     * Fichiers uploadés (zip, pdf, png, mp4…)
     */
    private List<MultipartFile> fichiers;

    /**
     * Liens externes (GitHub, Drive, Figma…)
     */
    private List<String> liensExternes;

    @NotNull
    private Long missionId;

    // ======== Getters / Setters ========
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<MultipartFile> getFichiers() { return fichiers; }
    public void setFichiers(List<MultipartFile> fichiers) { this.fichiers = fichiers; }

    public List<String> getLiensExternes() { return liensExternes; }
    public void setLiensExternes(List<String> liensExternes) { this.liensExternes = liensExternes; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
}
