package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.projet.freelencetinder.enum1.AuthProvider;

//import enums.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Représente un utilisateur (freelance, client ou admin).
 * Optimisé pour le système de swipe/matching.
 */
@Entity
@Table(
    name = "utilisateur",
    indexes = {
        @Index(name = "idx_user_type",           columnList = "typeUtilisateur"),
        @Index(name = "idx_client_subtype",      columnList = "typeClient"),
        @Index(name = "idx_user_email",          columnList = "email"),
        @Index(name = "idx_user_localisation",   columnList = "localisation"),
        /* ====== AJOUTS : index utiles pour le matching/filtrage ====== */
        @Index(name = "idx_user_disponibilite",  columnList = "disponibilite"),
        @Index(name = "idx_user_mobilite",       columnList = "mobilite"),
        @Index(name = "idx_user_timezone",       columnList = "timezone"),
        @Index(name = "idx_user_gouvernorat",    columnList = "gouvernorat") // *** AJOUT ***
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Utilisateur {

    /* ===================== Identité & Version ===================== */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking pour éviter écrasement concurrent. */
    @Version
    @Column(nullable = false)
    private Long version;

    /* ===================== Informations personnelles ===================== */
    @NotBlank @Size(max = 80)  private String nom;
    @NotBlank @Size(max = 80)  private String prenom;

    /* ===================== Authentification ===================== */
    @Email @NotBlank @Size(max = 160)
    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank @Size(min = 60, max = 100)
    @Column(nullable = false)
    private String motDePasse;

    /* ===================== Contact & profil ===================== */
    @Size(max = 30)   private String numeroTelephone;
    @Size(max = 500)  private String photoProfilUrl;

    /* ===================== Type d’utilisateur ===================== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TypeUtilisateur typeUtilisateur;

    /* Sous-catégorisation pour un client */
    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private TypeClient typeClient; // null sauf si CLIENT

    /* ===================== Audit ===================== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereConnexion;
    @Column(nullable = false) private boolean estActif = true;
    private LocalDateTime derniereMiseAJour;

    /* ===================== Préférences ===================== */
    @Enumerated(EnumType.STRING) private Langue languePref;

    /* ===================== Dimensions FREELANCE ===================== */
    @ElementCollection
    @CollectionTable(name = "utilisateur_competences",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "competence", length = 120)
    private Set<String> competences = new HashSet<>();

    @Positive private Double tarifHoraire;
    @Positive private Double tarifJournalier;
    @Enumerated(EnumType.STRING) private Disponibilite disponibilite;
    @Size(max = 1000) private String bio;
    @Enumerated(EnumType.STRING) private NiveauExperience niveauExperience;
    @Size(max = 160) private String localisation;

    /** Gouvernorat (lié à l'utilisateur, pas à la mission) */
    @Enumerated(EnumType.STRING)
    private Mission.Gouvernorat gouvernorat; // *** AJOUT ***

    @ElementCollection(targetClass = Mission.Categorie.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "utilisateur_categories",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "categorie", nullable = false, length = 40)
    private Set<Mission.Categorie> categories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "utilisateur_portfolio",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "url", length = 600)
    private List<String> portfolioUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "utilisateur_badges",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "badge", length = 120)
    private Set<String> listeBadges = new HashSet<>();

    @DecimalMin("0.0") @DecimalMax("5.0") private Double noteMoyenne;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer nombreAvis = 0; // *** AJOUT ***
    private Integer projetsTermines;

    /* ===================== Dimensions CLIENT ===================== */
    @Size(max = 200)  private String nomEntreprise;
    @Size(max = 300)  private String siteEntreprise;
    @Size(max = 1000) private String descriptionEntreprise;
    private Integer missionsPubliees;

    @ElementCollection
    @CollectionTable(name = "utilisateur_historique_missions",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "mission", length = 200)
    private List<String> historiqueMissions = new ArrayList<>();

    @DecimalMin("0.0") @DecimalMax("5.0") private Double noteDonneeMoy;

    /* ===================== Finance / Escrow ===================== */
    @Column(precision = 14, scale = 2) private BigDecimal soldeEscrow;

    /* ===================== Swiping & Gamification ===================== */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer nombreSwipes = 0;
    private LocalDateTime dernierSwipeAt;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer likesRecus = 0;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer matchesObtenus = 0;

    /* ===================== Push notifications ===================== */
    @ElementCollection
    @CollectionTable(name = "utilisateur_tokens_push",
                     joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "token", length = 300)
    @JsonIgnore
    private Set<String> pushTokens = new HashSet<>();

    /* ===================== Associations ===================== */
    @OneToMany(mappedBy = "client")              @JsonIgnore
    private List<Mission> missionsPublieesList = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceSelectionne") @JsonIgnore
    private List<Mission> missionsEnCours = new ArrayList<>();

    @OneToMany(mappedBy = "client")    @JsonIgnore
    private List<TranchePaiement> tranchesClient = new ArrayList<>();

    @OneToMany(mappedBy = "freelance") @JsonIgnore
    private List<TranchePaiement> tranchesFreelance = new ArrayList<>();

    @OneToMany(mappedBy = "freelancer") @JsonIgnore
    private List<Livrable> livrablesEnvoyes = new ArrayList<>();

    /* ===================================================================== */
    /* ===================== AJOUTS POUR CARTES & SWIPE ===================== */
    /* ===================================================================== */

    /* -------- Profil / headline / seniorité -------- */
    @Size(max = 140) private String titreProfil;                  // ex. "Ingénieur Backend Java/Angular"
    private Integer anneesExperience;                              // total années d'exp

    /* -------- Localisation et disponibilité avancée -------- */
    @Size(max = 60)  private String timezone = "Africa/Tunis";     // ex. Africa/Tunis
    @Enumerated(EnumType.STRING) private Mobilite mobilite = Mobilite.REMOTE;
    private LocalDate dateDisponibilite;                           // date de début possible
    @Min(0) @Max(7) private Integer chargeHebdoSouhaiteeJours;     // j/sem. souhaités

    /* Langues avec niveaux (pour la carte) */
    @ElementCollection
    @CollectionTable(name = "utilisateur_langues",
            joinColumns = @JoinColumn(name = "utilisateur_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "niveau_langue", length = 10)
    @Enumerated(EnumType.STRING)
    private Map<Langue, NiveauLangue> langues = new HashMap<>();

    /* Compétences avec niveau (en plus du Set<String> existant) */
    @ElementCollection
    @CollectionTable(name = "utilisateur_competences_niveaux",
            joinColumns = @JoinColumn(name = "utilisateur_id"))
    @MapKeyColumn(name = "competence", length = 120)
    @Column(name = "niveau", length = 12)
    @Enumerated(EnumType.STRING)
    private Map<String, NiveauMaitrise> competencesNiveaux = new HashMap<>();

    /* Modèles d’engagement préférés & flexibilité tarifaire */
    @ElementCollection(targetClass = EngagementModel.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "utilisateur_engagement_models",
            joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "modele", length = 12)
    private Set<EngagementModel> modelesEngagementPreferes = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private PreferenceDuree preferenceDuree = PreferenceDuree.INDIFFERENT; // *** AJOUT ***

    @DecimalMin("0.0") @DecimalMax("100.0")
    private Double flexibiliteTarifairePourcent; // ex. ±10%

    /* Vérifications / conformité (pour badges “Vérifié”) */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean emailVerifie = false;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean telephoneVerifie = false;
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean identiteVerifiee = false; // KYC pièce d’identité
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean ribVerifie = false;       // RIB/IBAN validé
    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private StatutKyc kycStatut = StatutKyc.NON_DEMARRE;

    /* Indicateurs qualité/réputation (pour carte client) */
    @DecimalMin("0.0") @DecimalMax("100.0") private Double tauxReussite;      // %
    @DecimalMin("0.0") @DecimalMax("100.0") private Double tauxRespectDelais; // %
    @DecimalMin("0.0") @DecimalMax("100.0") private Double tauxReembauche;    // %
    private Integer delaiReponseHeures;                                        // moyenne en h
    private Integer delaiReponseMedianMinutes;                                 // médiane en min

    /* Certifications (affichage carte/modale) */
    @ElementCollection
    @CollectionTable(name = "utilisateur_certifications",
            joinColumns = @JoinColumn(name = "utilisateur_id"))
    @Column(name = "certif", length = 160)
    private List<String> certifications = new ArrayList<>();

    /* Réseaux pro (icônes sur la carte) */
    @Size(max = 250) private String linkedinUrl; // *** AJOUT ***
    @Size(max = 250) private String githubUrl;   // *** AJOUT ***

    /* Préférences & garde-fous côté messagerie/flux */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean autoriseContactAvantMatch = false; // politique: en général false

    /* Quotas & gamification avancée */
    @Column(nullable = false, columnDefinition = "integer default 30")
    private Integer quotaSwipesQuotidien = 30;
    private LocalDateTime quotaSwipesDernierReset;
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer superLikesRestantsDuJour = 0;
    private LocalDateTime dernierSuperLikeAt;

    /* Santé du compte (modération) */
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer signalementsRecus = 0;
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double suspicionFraudeScore; // 0..1

    /* Fiabilité paiement côté client (pour rassurer freelances) */
    private Integer delaiPaiementMoyenJours;         // ex. 7 jours
    @DecimalMin("0.0") @DecimalMax("100.0")
    private Double fiabilitePaiement;                // %
    @Size(max = 60) private String prestataireEscrowFavori; // ex. Paymee
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean exigeDepotAvantChat = false;
    
    @Enumerated(EnumType.STRING)
	private AuthProvider authProvider;

    /* ===================== Hooks JPA ===================== */
    @PrePersist
    protected void onCreate() {
        this.dateCreation      = LocalDateTime.now();
        this.derniereMiseAJour = this.dateCreation;
        if (soldeEscrow     == null) soldeEscrow     = BigDecimal.ZERO;
        if (nombreSwipes    == null) nombreSwipes    = 0;
        if (likesRecus      == null) likesRecus      = 0;
        if (matchesObtenus  == null) matchesObtenus  = 0;
        if (nombreAvis      == null) nombreAvis      = 0; // *** AJOUT ***
        /* AJOUTS : init quotas et resets */
        if (quotaSwipesDernierReset == null) quotaSwipesDernierReset = this.dateCreation;
        if (superLikesRestantsDuJour == null) superLikesRestantsDuJour = 0;
    }
    @PreUpdate
    protected void onUpdate() { this.derniereMiseAJour = LocalDateTime.now(); }

    /* ===================== Méthodes utilitaires ===================== */
    public void incrementNombreSwipes()   {
        if (nombreSwipes == null) nombreSwipes = 0;
        nombreSwipes++;
        this.dernierSwipeAt = LocalDateTime.now();
    }
    public void incrementLikesRecus() {
        if (likesRecus == null) likesRecus = 0;
        likesRecus++;
    }
    public void incrementMatchesObtenus() {
        if (matchesObtenus == null) matchesObtenus = 0;
        matchesObtenus++;
    }
    public String getNomComplet() {
        return (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
    }

    /* ===================== Getters & Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    
    
    public AuthProvider getAuthProvider() {
		return authProvider;
	}
	public void setAuthProvider(AuthProvider authProvider) {
		this.authProvider = authProvider;
	}
	public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    public String getPhotoProfilUrl() { return photoProfilUrl; }
    public void setPhotoProfilUrl(String photoProfilUrl) { this.photoProfilUrl = photoProfilUrl; }

    public TypeUtilisateur getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(TypeUtilisateur typeUtilisateur) { this.typeUtilisateur = typeUtilisateur; }

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDerniereConnexion() { return dateDerniereConnexion; }
    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) { this.dateDerniereConnexion = dateDerniereConnexion; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) { this.derniereMiseAJour = derniereMiseAJour; }

    public Langue getLanguePref() { return languePref; }
    public void setLanguePref(Langue languePref) { this.languePref = languePref; }

    public Set<String> getCompetences() { return competences; }
    public void setCompetences(Set<String> competences) { this.competences = competences; }

    public Double getTarifHoraire() { return tarifHoraire; }
    public void setTarifHoraire(Double tarifHoraire) { this.tarifHoraire = tarifHoraire; }

    public Double getTarifJournalier() { return tarifJournalier; }
    public void setTarifJournalier(Double tarifJournalier) { this.tarifJournalier = tarifJournalier; }

    public Disponibilite getDisponibilite() { return disponibilite; }
    public void setDisponibilite(Disponibilite disponibilite) { this.disponibilite = disponibilite; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public NiveauExperience getNiveauExperience() { return niveauExperience; }
    public void setNiveauExperience(NiveauExperience niveauExperience) { this.niveauExperience = niveauExperience; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Mission.Gouvernorat getGouvernorat() { return gouvernorat; }                  // *** AJOUT ***
    public void setGouvernorat(Mission.Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; } // *** AJOUT ***

    public Set<Mission.Categorie> getCategories() { return categories; }
    public void setCategories(Set<Mission.Categorie> categories) { this.categories = categories; }

    public List<String> getPortfolioUrls() { return portfolioUrls; }
    public void setPortfolioUrls(List<String> portfolioUrls) { this.portfolioUrls = portfolioUrls; }

    public Set<String> getListeBadges() { return listeBadges; }
    public void setListeBadges(Set<String> listeBadges) { this.listeBadges = listeBadges; }

    public Double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(Double noteMoyenne) { this.noteMoyenne = noteMoyenne; }

    public Integer getNombreAvis() { return nombreAvis; }               // *** AJOUT ***
    public void setNombreAvis(Integer nombreAvis) { this.nombreAvis = nombreAvis; } // *** AJOUT ***

    public Integer getProjetsTermines() { return projetsTermines; }
    public void setProjetsTermines(Integer projetsTermines) { this.projetsTermines = projetsTermines; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getSiteEntreprise() { return siteEntreprise; }
    public void setSiteEntreprise(String siteEntreprise) { this.siteEntreprise = siteEntreprise; }

    public String getDescriptionEntreprise() { return descriptionEntreprise; }
    public void setDescriptionEntreprise(String descriptionEntreprise) { this.descriptionEntreprise = descriptionEntreprise; }

    public Integer getMissionsPubliees() { return missionsPubliees; }
    public void setMissionsPubliees(Integer missionsPubliees) { this.missionsPubliees = missionsPubliees; }

    public List<String> getHistoriqueMissions() { return historiqueMissions; }
    public void setHistoriqueMissions(List<String> historiqueMissions) { this.historiqueMissions = historiqueMissions; }

    public Double getNoteDonneeMoy() { return noteDonneeMoy; }
    public void setNoteDonneeMoy(Double noteDonneeMoy) { this.noteDonneeMoy = noteDonneeMoy; }

    public BigDecimal getSoldeEscrow() { return soldeEscrow; }
    public void setSoldeEscrow(BigDecimal soldeEscrow) { this.soldeEscrow = soldeEscrow; }

    public Integer getNombreSwipes() { return nombreSwipes; }
    public void setNombreSwipes(Integer nombreSwipes) { this.nombreSwipes = nombreSwipes; }

    public LocalDateTime getDernierSwipeAt() { return dernierSwipeAt; }
    public void setDernierSwipeAt(LocalDateTime dernierSwipeAt) { this.dernierSwipeAt = dernierSwipeAt; }

    public Integer getLikesRecus() { return likesRecus; }
    public void setLikesRecus(Integer likesRecus) { this.likesRecus = likesRecus; }

    public Integer getMatchesObtenus() { return matchesObtenus; }
    public void setMatchesObtenus(Integer matchesObtenus) { this.matchesObtenus = matchesObtenus; }

    public Set<String> getPushTokens() { return pushTokens; }
    public void setPushTokens(Set<String> pushTokens) { this.pushTokens = pushTokens; }

    public List<Mission> getMissionsPublieesList() { return missionsPublieesList; }
    public void setMissionsPublieesList(List<Mission> missionsPublieesList) { this.missionsPublieesList = missionsPublieesList; }

    public List<Mission> getMissionsEnCours() { return missionsEnCours; }
    public void setMissionsEnCours(List<Mission> missionsEnCours) { this.missionsEnCours = missionsEnCours; }

    public List<TranchePaiement> getTranchesClient() { return tranchesClient; }
    public void setTranchesClient(List<TranchePaiement> tranchesClient) { this.tranchesClient = tranchesClient; }

    public List<TranchePaiement> getTranchesFreelance() { return tranchesFreelance; }
    public void setTranchesFreelance(List<TranchePaiement> tranchesFreelance) { this.tranchesFreelance = tranchesFreelance; }

    public List<Livrable> getLivrablesEnvoyes() { return livrablesEnvoyes; }
    public void setLivrablesEnvoyes(List<Livrable> livrablesEnvoyes) { this.livrablesEnvoyes = livrablesEnvoyes; }

    /* ===== Getters/Setters AJOUTS ===== */
    public String getTitreProfil() { return titreProfil; }
    public void setTitreProfil(String titreProfil) { this.titreProfil = titreProfil; }

    public Integer getAnneesExperience() { return anneesExperience; }
    public void setAnneesExperience(Integer anneesExperience) { this.anneesExperience = anneesExperience; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Mobilite getMobilite() { return mobilite; }
    public void setMobilite(Mobilite mobilite) { this.mobilite = mobilite; }

    public LocalDate getDateDisponibilite() { return dateDisponibilite; }
    public void setDateDisponibilite(LocalDate dateDisponibilite) { this.dateDisponibilite = dateDisponibilite; }

    public Integer getChargeHebdoSouhaiteeJours() { return chargeHebdoSouhaiteeJours; }
    public void setChargeHebdoSouhaiteeJours(Integer chargeHebdoSouhaiteeJours) { this.chargeHebdoSouhaiteeJours = chargeHebdoSouhaiteeJours; }

    public Map<Langue, NiveauLangue> getLangues() { return langues; }
    public void setLangues(Map<Langue, NiveauLangue> langues) { this.langues = langues; }

    public Map<String, NiveauMaitrise> getCompetencesNiveaux() { return competencesNiveaux; }
    public void setCompetencesNiveaux(Map<String, NiveauMaitrise> competencesNiveaux) { this.competencesNiveaux = competencesNiveaux; }

    public Set<EngagementModel> getModelesEngagementPreferes() { return modelesEngagementPreferes; }
    public void setModelesEngagementPreferes(Set<EngagementModel> modelesEngagementPreferes) { this.modelesEngagementPreferes = modelesEngagementPreferes; }

    public PreferenceDuree getPreferenceDuree() { return preferenceDuree; } // *** AJOUT ***
    public void setPreferenceDuree(PreferenceDuree preferenceDuree) { this.preferenceDuree = preferenceDuree; } // *** AJOUT ***

    public Double getFlexibiliteTarifairePourcent() { return flexibiliteTarifairePourcent; }
    public void setFlexibiliteTarifairePourcent(Double flexibiliteTarifairePourcent) { this.flexibiliteTarifairePourcent = flexibiliteTarifairePourcent; }

    public boolean isEmailVerifie() { return emailVerifie; }
    public void setEmailVerifie(boolean emailVerifie) { this.emailVerifie = emailVerifie; }

    public boolean isTelephoneVerifie() { return telephoneVerifie; }
    public void setTelephoneVerifie(boolean telephoneVerifie) { this.telephoneVerifie = telephoneVerifie; }

    public boolean isIdentiteVerifiee() { return identiteVerifiee; }
    public void setIdentiteVerifiee(boolean identiteVerifiee) { this.identiteVerifiee = identiteVerifiee; }

    public boolean isRibVerifie() { return ribVerifie; }
    public void setRibVerifie(boolean ribVerifie) { this.ribVerifie = ribVerifie; }

    public StatutKyc getKycStatut() { return kycStatut; }
    public void setKycStatut(StatutKyc kycStatut) { this.kycStatut = kycStatut; }

    public Double getTauxReussite() { return tauxReussite; }
    public void setTauxReussite(Double tauxReussite) { this.tauxReussite = tauxReussite; }

    public Double getTauxRespectDelais() { return tauxRespectDelais; }
    public void setTauxRespectDelais(Double tauxRespectDelais) { this.tauxRespectDelais = tauxRespectDelais; }

    public Double getTauxReembauche() { return tauxReembauche; }
    public void setTauxReembauche(Double tauxReembauche) { this.tauxReembauche = tauxReembauche; }

    public Integer getDelaiReponseHeures() { return delaiReponseHeures; }
    public void setDelaiReponseHeures(Integer delaiReponseHeures) { this.delaiReponseHeures = delaiReponseHeures; }

    public Integer getDelaiReponseMedianMinutes() { return delaiReponseMedianMinutes; }
    public void setDelaiReponseMedianMinutes(Integer delaiReponseMedianMinutes) { this.delaiReponseMedianMinutes = delaiReponseMedianMinutes; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public String getLinkedinUrl() { return linkedinUrl; }   // *** AJOUT ***
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; } // *** AJOUT ***

    public String getGithubUrl() { return githubUrl; }       // *** AJOUT ***
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; } // *** AJOUT ***

    public boolean isAutoriseContactAvantMatch() { return autoriseContactAvantMatch; }
    public void setAutoriseContactAvantMatch(boolean autoriseContactAvantMatch) { this.autoriseContactAvantMatch = autoriseContactAvantMatch; }

    public Integer getQuotaSwipesQuotidien() { return quotaSwipesQuotidien; }
    public void setQuotaSwipesQuotidien(Integer quotaSwipesQuotidien) { this.quotaSwipesQuotidien = quotaSwipesQuotidien; }

    public LocalDateTime getQuotaSwipesDernierReset() { return quotaSwipesDernierReset; }
    public void setQuotaSwipesDernierReset(LocalDateTime quotaSwipesDernierReset) { this.quotaSwipesDernierReset = quotaSwipesDernierReset; }

    public Integer getSuperLikesRestantsDuJour() { return superLikesRestantsDuJour; }
    public void setSuperLikesRestantsDuJour(Integer superLikesRestantsDuJour) { this.superLikesRestantsDuJour = superLikesRestantsDuJour; }

    public LocalDateTime getDernierSuperLikeAt() { return dernierSuperLikeAt; }
    public void setDernierSuperLikeAt(LocalDateTime dernierSuperLikeAt) { this.dernierSuperLikeAt = dernierSuperLikeAt; }

    public Integer getSignalementsRecus() { return signalementsRecus; }
    public void setSignalementsRecus(Integer signalementsRecus) { this.signalementsRecus = signalementsRecus; }

    public Double getSuspicionFraudeScore() { return suspicionFraudeScore; }
    public void setSuspicionFraudeScore(Double suspicionFraudeScore) { this.suspicionFraudeScore = suspicionFraudeScore; }

    public Integer getDelaiPaiementMoyenJours() { return delaiPaiementMoyenJours; }
    public void setDelaiPaiementMoyenJours(Integer delaiPaiementMoyenJours) { this.delaiPaiementMoyenJours = delaiPaiementMoyenJours; }

    public Double getFiabilitePaiement() { return fiabilitePaiement; }
    public void setFiabilitePaiement(Double fiabilitePaiement) { this.fiabilitePaiement = fiabilitePaiement; }

    public String getPrestataireEscrowFavori() { return prestataireEscrowFavori; }
    public void setPrestataireEscrowFavori(String prestataireEscrowFavori) { this.prestataireEscrowFavori = prestataireEscrowFavori; }

    public boolean isExigeDepotAvantChat() { return exigeDepotAvantChat; }
    public void setExigeDepotAvantChat(boolean exigeDepotAvantChat) { this.exigeDepotAvantChat = exigeDepotAvantChat; }

    /* ===================== Enums ===================== */
    public enum TypeUtilisateur { FREELANCE, CLIENT, ADMIN }

    /** Sous-catégorisation du client */
    public enum TypeClient {
        PME_STARTUP,
        ENTREPRENEUR,
        ETUDIANT_PARTICULIER,
        CLIENT_ETRANGER
    }

    public enum Disponibilite    { TEMPS_PLEIN, TEMPS_PARTIEL, PONCTUEL, INDISPONIBLE }
    public enum NiveauExperience { DEBUTANT, INTERMEDIAIRE, EXPERT }
    public enum Langue           { FR, AR, EN }

    /* ===== AJOUTS : enums complémentaires ===== */
    public enum Mobilite { REMOTE, ONSITE, BOTH }
    public enum NiveauLangue { A1, A2, B1, B2, C1, C2, NATIF }
    public enum NiveauMaitrise { DEBUTANT, INTERMEDIAIRE, AVANCE, EXPERT }
    public enum EngagementModel { FORFAIT, TJM, REGIE }
    public enum PreferenceDuree { COURT_TERME, LONG_TERME, INDIFFERENT } // *** AJOUT ***
    public enum StatutKyc { NON_DEMARRE, EN_COURS, VERIFIE, REJETE }
}
