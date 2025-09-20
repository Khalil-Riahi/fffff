package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Swipe côté freelance sur une mission.
 * Ajouts: contexte Tunisie (gouvernorat), super-like, raisons de rejet, source, métadonnées UX.
 */
@Entity
@Table(
    name = "swipe",
    uniqueConstraints = @UniqueConstraint(name = "uk_swipe_freelance_mission",
        columnNames = { "freelance_id", "mission_id" }),
    indexes = {
        @Index(name = "idx_swipe_freelance", columnList = "freelance_id"),
        @Index(name = "idx_swipe_mission", columnList = "mission_id"),
        @Index(name = "idx_swipe_decision", columnList = "decision"),
        /* ===== AJOUTS : index utiles ===== */
        @Index(name = "idx_swipe_source", columnList = "source"),
        @Index(name = "idx_swipe_superlike", columnList = "super_like"),
        @Index(name = "idx_swipe_session", columnList = "session_id"),
        @Index(name = "idx_swipe_gouv", columnList = "gouvernorat_freelance")
    }
)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking */
    @Version
    private long version; // primitive => jamais null

    /* ============== Relations ============== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    /* ============== Données ============== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Decision decision; // AJOUT possible: SUPERLIKE (voir enum plus bas)

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateSwipe;

    private Long dwellTimeMs;

    @Column(name = "a_genere_match", nullable = false, columnDefinition = "boolean default false")
    private Boolean aGenereMatch = false;

    /* ============== AJOUTS ============== */

    /** L’instant où la carte a été affichée (pour mesurer la latence/lecture) */
    private LocalDateTime dateAffichage;

    /** Latence entre affichage et swipe (ms) — utile analytics UX */
    private Long latenceAffichageMs;

    /** Rang de la carte dans la pile au moment du swipe (0 = tout en haut) */
    private Integer positionDansPile;

    /** Session côté front (pour regrouper les swipes d’une même “session”) */
    @Size(max = 64)
    @Column(name = "session_id", length = 64)
    private String sessionId;

    /** Source du swipe: ANDROID/IOS/WEB etc. */
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Source source = Source.WEB;

    /** Langue de l’interface au moment du swipe (FR/AR/EN) */
    @Enumerated(EnumType.STRING)
    private Utilisateur.Langue langueUi;

    /** Gouvernorat du freelance au moment du swipe (Tunisie) */
    @Enumerated(EnumType.STRING)
    @Column(name = "gouvernorat_freelance", length = 30)
    private Mission.Gouvernorat gouvernoratFreelance;

    /** Marqueur super-like rapide (en plus de Decision.SUPERLIKE pour filtrage) */
    @Column(name = "super_like", nullable = false, columnDefinition = "boolean default false")
    private Boolean superLike = false;

    /** Raison (optionnelle) d’un DISLIKE — utile pour améliorer le matching */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RaisonRejet raisonRejet;

    /** Champ libre si raison = AUTRE (limitée en longueur) */
    @Size(max = 200)
    private String raisonRejetDetails;

    /** Message d’intro automatique envoyé côté freelance lors d’un LIKE/SUPERLIKE */
    @Size(max = 600)
    private String messageIntroAuto;

    /** Version de l’algorithme de ranking/matching au moment du swipe */
    @Size(max = 40)
    private String algoVersion;

    /** Indique si ce swipe est un “undo/retour” (annulation du swipe précédent) */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean estUndo = false;

    /** Référence facultative de l’id du swipe annulé (pas de FK pour rester simple) */
    private Long annuleSwipeId;

    /** Hachage d’IP (ou autre identifiant) pour modération/abus, sans PII en clair */
    @Size(max = 128)
    private String ipHash;

    /** Campagne/expérimentation (A/B) facultative côté front */
    @Size(max = 60)
    private String campagneAb;

    @PrePersist
    protected void onCreate() {
        this.dateSwipe = LocalDateTime.now();
        if (aGenereMatch == null) aGenereMatch = false;
        if (superLike == null) superLike = false;
        if (dateAffichage == null) dateAffichage = this.dateSwipe;
        if (langueUi == null) langueUi = Utilisateur.Langue.FR; // défaut Tunisie
        if (source == null) source = Source.WEB;
    }

    /* ============== Getters / Setters ============== */
    public Long getId() { return id; }

    public long getVersion() { return version; } // pas de setter public

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public Boolean getAGenereMatch() { return aGenereMatch; }
    public void setAGenereMatch(Boolean aGenereMatch) {
        this.aGenereMatch = (aGenereMatch == null ? false : aGenereMatch);
    }

    public LocalDateTime getDateAffichage() { return dateAffichage; }
    public void setDateAffichage(LocalDateTime dateAffichage) { this.dateAffichage = dateAffichage; }

    public Long getLatenceAffichageMs() { return latenceAffichageMs; }
    public void setLatenceAffichageMs(Long latenceAffichageMs) { this.latenceAffichageMs = latenceAffichageMs; }

    public Integer getPositionDansPile() { return positionDansPile; }
    public void setPositionDansPile(Integer positionDansPile) { this.positionDansPile = positionDansPile; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public Utilisateur.Langue getLangueUi() { return langueUi; }
    public void setLangueUi(Utilisateur.Langue langueUi) { this.langueUi = langueUi; }

    public Mission.Gouvernorat getGouvernoratFreelance() { return gouvernoratFreelance; }
    public void setGouvernoratFreelance(Mission.Gouvernorat gouvernoratFreelance) { this.gouvernoratFreelance = gouvernoratFreelance; }

    public Boolean getSuperLike() { return superLike; }
    public void setSuperLike(Boolean superLike) { this.superLike = (superLike == null ? false : superLike); }

    public RaisonRejet getRaisonRejet() { return raisonRejet; }
    public void setRaisonRejet(RaisonRejet raisonRejet) { this.raisonRejet = raisonRejet; }

    public String getRaisonRejetDetails() { return raisonRejetDetails; }
    public void setRaisonRejetDetails(String raisonRejetDetails) { this.raisonRejetDetails = raisonRejetDetails; }

    public String getMessageIntroAuto() { return messageIntroAuto; }
    public void setMessageIntroAuto(String messageIntroAuto) { this.messageIntroAuto = messageIntroAuto; }

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

    /* ============== Enums ============== */
    public enum Decision { LIKE, DISLIKE, SUPERLIKE } // AJOUT: SUPERLIKE

    public enum Source { WEB, ANDROID, IOS }

    /** Raisons de rejet (DISLIKE) – focalisées MVP Tunisie */
    public enum RaisonRejet {
        BUDGET,               // Budget trop bas / hors fourchette
        COMPETENCES,          // Stack/skills non alignés
        DISPONIBILITE,        // Timing/charge incompatible
        LOCALISATION,         // Gouvernorat / sur site non possible
        LANGUE,               // FR/AR/EN non adaptés
        QUALITE_BRIEF,        // Brief insuffisant
        AUTRE                 // Saisie libre
    }
}
