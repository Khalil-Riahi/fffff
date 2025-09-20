package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // AJOUT

@Entity
@Table(
    name = "client_swipe",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_client_swipe_triplet",
        columnNames = { "client_id", "mission_id", "freelance_id" }
    ),
    indexes = {
        @Index(name = "idx_client_swipe_client", columnList = "client_id"),
        @Index(name = "idx_client_swipe_mission", columnList = "mission_id"),
        @Index(name = "idx_client_swipe_freelance", columnList = "freelance_id"),
        @Index(name = "idx_client_swipe_decision", columnList = "decision"),
        /* ===================== AJOUTS : index utiles ===================== */
        @Index(name = "idx_client_swipe_source", columnList = "source"),
        @Index(name = "idx_client_swipe_superinvite", columnList = "super_invite"),
        @Index(name = "idx_client_swipe_session", columnList = "session_id"),
        @Index(name = "idx_client_swipe_gouv", columnList = "gouvernorat_client")
    }
)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ClientSwipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    /* ============== Relations ============== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    /* ============== Données ============== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Swipe.Decision decision;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSwipe;

    @Column(name = "a_genere_match", nullable = false, columnDefinition = "boolean default false")
    private Boolean aGenereMatch = false;

    private Long dwellTimeMs;

    /* ===================== AJOUTS ===================== */

    /** Date d’affichage de la carte avant action (pour analytics) */
    private LocalDateTime dateAffichage;

    /** Latence (ms) entre affichage et swipe */
    private Long latenceAffichageMs;

    /** Rang de la carte dans la pile (0 = top) */
    private Integer positionDansPile;

    /** Session front pour regrouper les swipes */
    @Size(max = 64)
    @Column(name = "session_id", length = 64)
    private String sessionId;

    /** Source (WEB/ANDROID/IOS) */
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Swipe.Source source = Swipe.Source.WEB;

    /** Langue UI au moment du swipe (FR/AR/EN) — défaut Tunisie: FR */
    @Enumerated(EnumType.STRING)
    private Utilisateur.Langue langueUi;

    /** Gouvernorat (Tunisie) côté client/entreprise au moment du swipe */
    @Enumerated(EnumType.STRING)
    @Column(name = "gouvernorat_client", length = 30)
    private Mission.Gouvernorat gouvernoratClient;

    /** Marqueur “super-invite” (équivalent client du super-like) */
    @Column(name = "super_invite", nullable = false, columnDefinition = "boolean default false")
    private Boolean superInvite = false;

    /** Raison (optionnelle) d’un DISLIKE par le client */
    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private ClientRaisonRejet raisonRejet;

    /** Détail libre si raison = AUTRE */
    @Size(max = 200)
    private String raisonRejetDetails;

    /** Message d’invitation auto envoyé au freelance lors d’un LIKE/SUPERLIKE */
    @Size(max = 600)
    private String messageInviteAuto;

    /** Version de l’algo de ranking/matching au moment du swipe */
    @Size(max = 40)
    private String algoVersion;

    /** Indique si ce swipe est un undo (annulation) */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean estUndo = false;

    /** Id du swipe annulé (pas de FK stricte pour simplicité) */
    private Long annuleSwipeId;

    /** Hachage d’IP (ou identifiant) pour modération/abus — pas de PII brute */
    @Size(max = 128)
    private String ipHash;

    /** Tag d’expérimentation A/B côté front */
    @Size(max = 60)
    private String campagneAb;

    @PrePersist
    protected void onCreate() {
        this.dateSwipe = LocalDateTime.now();
        if (aGenereMatch == null) aGenereMatch = false;
        if (superInvite == null) superInvite = false;
        if (dateAffichage == null) dateAffichage = this.dateSwipe;
        if (langueUi == null) langueUi = Utilisateur.Langue.FR; // défaut Tunisie
        if (source == null) source = Swipe.Source.WEB;
    }

    /* ============== Getters / Setters ============== */
    public Long getId() { return id; }

    public long getVersion() { return version; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public Swipe.Decision getDecision() { return decision; }
    public void setDecision(Swipe.Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public Boolean getAGenereMatch() { return aGenereMatch; }
    public void setAGenereMatch(Boolean aGenereMatch) {
        this.aGenereMatch = (aGenereMatch == null ? false : aGenereMatch);
    }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public LocalDateTime getDateAffichage() { return dateAffichage; }
    public void setDateAffichage(LocalDateTime dateAffichage) { this.dateAffichage = dateAffichage; }

    public Long getLatenceAffichageMs() { return latenceAffichageMs; }
    public void setLatenceAffichageMs(Long latenceAffichageMs) { this.latenceAffichageMs = latenceAffichageMs; }

    public Integer getPositionDansPile() { return positionDansPile; }
    public void setPositionDansPile(Integer positionDansPile) { this.positionDansPile = positionDansPile; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Swipe.Source getSource() { return source; }
    public void setSource(Swipe.Source source) { this.source = source; }

    public Utilisateur.Langue getLangueUi() { return langueUi; }
    public void setLangueUi(Utilisateur.Langue langueUi) { this.langueUi = langueUi; }

    public Mission.Gouvernorat getGouvernoratClient() { return gouvernoratClient; }
    public void setGouvernoratClient(Mission.Gouvernorat gouvernoratClient) { this.gouvernoratClient = gouvernoratClient; }

    public Boolean getSuperInvite() { return superInvite; }
    public void setSuperInvite(Boolean superInvite) { this.superInvite = (superInvite == null ? false : superInvite); }

    public ClientRaisonRejet getRaisonRejet() { return raisonRejet; }
    public void setRaisonRejet(ClientRaisonRejet raisonRejet) { this.raisonRejet = raisonRejet; }

    public String getRaisonRejetDetails() { return raisonRejetDetails; }
    public void setRaisonRejetDetails(String raisonRejetDetails) { this.raisonRejetDetails = raisonRejetDetails; }

    public String getMessageInviteAuto() { return messageInviteAuto; }
    public void setMessageInviteAuto(String messageInviteAuto) { this.messageInviteAuto = messageInviteAuto; }

    public String getAlgoVersion() { return algoVersion; }
    public void setAlgoVersion(String algoVersion) { this.algoVersion = algoVersion; }

    public Boolean getEstUndo() { return estUndo; }
    public void setEstUndo(Boolean estUndo) { this.estUndo = (estUndo == null ? false : estUndo); }

    public Long getAnnuleSwipeId() { return annuleSwipeId; }
    public void setAnnuleSwipeId(Long annuleSwipeId) { this.annuleSwipeId = annuleSwipeId; }

    public String getIpHash() { return ipHash; }
    public void setIpHash(String ipHash) { this.ipHash = ipHash; }

    public String getCampagneAb() { return campagneAb; }
    public void setCampagneAb(String campagneAb) { this.campagneAb = campagneAb; }

    /* ============== Enums additionnelles (client) ============== */

    /** Raisons de rejet côté client (DISLIKE) — contexte Tunisie/MVP */
    public enum ClientRaisonRejet {
        COUT_ELEVE,          // TJM/forfait perçu trop élevé
        EXPERIENCE_INSUFFISANTE,
        DISPONIBILITE,       // planning incompatible
        LOCALISATION,        // gouvernorat / présence sur site requise
        LANGUE,              // FR/AR/EN non adaptés au besoin
        ADEQUATION_CULTURE,  // fit d’équipe/soft skills
        QUALITE_PORTFOLIO,
        AUTRE
    }
}
