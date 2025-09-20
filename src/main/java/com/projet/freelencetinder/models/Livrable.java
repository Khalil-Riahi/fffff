package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Fichier ou ensemble de fichiers remis par le freelance pour une mission.
 */
@Entity
@Table(name = "livrable")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Livrable {

    /* ---------- Identité & version ---------- */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ---------- Contenu ---------- */
    @NotBlank @Size(max = 160)
    private String titre;

    @Size(max = 4000)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusLivrable status = StatusLivrable.EN_ATTENTE;

    /* ---------- Stockage ---------- */
    @ElementCollection
    @CollectionTable(name = "livrable_liens", joinColumns = @JoinColumn(name = "livrable_id"))
    @Column(name = "url", length = 600)
    private List<String> liensExternes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "livrable_fichiers", joinColumns = @JoinColumn(name = "livrable_id"))
    @Column(name = "chemin", length = 600)
    private List<String> cheminsFichiers = new ArrayList<>();

    /* ---------- Relations ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private Utilisateur freelancer;

    /* ---------- Hooks ---------- */
    @PrePersist
    protected void onCreate() {
        this.dateEnvoi = LocalDateTime.now();
    }

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public StatusLivrable getStatus() { return status; }
    public void setStatus(StatusLivrable status) { this.status = status; }
    public List<String> getLiensExternes() { return liensExternes; }
    public void setLiensExternes(List<String> liensExternes) { this.liensExternes = liensExternes; }
    public List<String> getCheminsFichiers() { return cheminsFichiers; }
    public void setCheminsFichiers(List<String> cheminsFichiers) { this.cheminsFichiers = cheminsFichiers; }
    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }
    public Utilisateur getFreelancer() { return freelancer; }
    public void setFreelancer(Utilisateur freelancer) { this.freelancer = freelancer; }
}
