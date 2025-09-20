package com.projet.freelencetinder.dto;

import java.util.Set;

import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Utilisateur.StatutKyc;
import com.projet.freelencetinder.models.Utilisateur.TypeClient;

/**
 * Informations publiques du client visibles par un freelance.
 */
public class ClientInfoDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String photoUrl;
    private String ville;

    /* Sous-type du client (PME, Entrepreneur, etc.) */
    private TypeClient typeClient;

    /* ======== AJOUTS (Tunisie & confiance) ======== */
    private Gouvernorat gouvernorat;     // ex. TUNIS, SFAX...
    private String timezone;             // ex. Africa/Tunis

    // Confiance / réputation côté client
    private Integer missionsPubliees;    // total pub.
    private Double  noteDonneeMoy;       // moyenne des notes données aux freelances
    private Double  fiabilitePaiement;   // %
    private Integer delaiPaiementMoyenJours;

    // Vérifications / KYC
    private boolean emailVerifie;
    private boolean telephoneVerifie;
    private boolean identiteVerifiee;
    private boolean ribVerifie;
    private StatutKyc kycStatut;

    // Entreprise
    private String nomEntreprise;
    private String siteEntreprise;
    private String descriptionEntreprise;

    // Badges client (ex. “Client vérifié”, “Paiement rapide”)
    private Set<String> badges;

    public ClientInfoDTO() {}

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public TypeClient getTypeClient() { return typeClient; }
    public void setTypeClient(TypeClient typeClient) { this.typeClient = typeClient; }

    public Gouvernorat getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(Gouvernorat gouvernorat) { this.gouvernorat = gouvernorat; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Integer getMissionsPubliees() { return missionsPubliees; }
    public void setMissionsPubliees(Integer missionsPubliees) { this.missionsPubliees = missionsPubliees; }

    public Double getNoteDonneeMoy() { return noteDonneeMoy; }
    public void setNoteDonneeMoy(Double noteDonneeMoy) { this.noteDonneeMoy = noteDonneeMoy; }

    public Double getFiabilitePaiement() { return fiabilitePaiement; }
    public void setFiabilitePaiement(Double fiabilitePaiement) { this.fiabilitePaiement = fiabilitePaiement; }

    public Integer getDelaiPaiementMoyenJours() { return delaiPaiementMoyenJours; }
    public void setDelaiPaiementMoyenJours(Integer delaiPaiementMoyenJours) { this.delaiPaiementMoyenJours = delaiPaiementMoyenJours; }

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

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getSiteEntreprise() { return siteEntreprise; }
    public void setSiteEntreprise(String siteEntreprise) { this.siteEntreprise = siteEntreprise; }

    public String getDescriptionEntreprise() { return descriptionEntreprise; }
    public void setDescriptionEntreprise(String descriptionEntreprise) { this.descriptionEntreprise = descriptionEntreprise; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }
}
