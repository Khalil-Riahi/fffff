// src/main/java/com/projet/freelencetinder/servcie/AuthService.java
package com.projet.freelencetinder.servcie;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projet.freelencetinder.dto.LoginRequest;
import com.projet.freelencetinder.dto.RefeshTokenRequest;
import com.projet.freelencetinder.dto.RegisterRequest;
import com.projet.freelencetinder.dto.TokenPair;
import com.projet.freelencetinder.enum1.AuthProvider;
import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.RefreshToken;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.models.Utilisateur.TypeClient;
import com.projet.freelencetinder.repository.UtilisateurRepository;

@Service
public class AuthService {

    @Autowired private UtilisateurRepository userRepo;
    @Autowired private PasswordEncoder       passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtService            jwtService;
    @Autowired private UserDetailsService    userDetailsService;
    @Autowired private RefreshTokenService   refreshTokenService;

    /* =================================================================
       Inscription + création du profil de base
       ================================================================= */
    @Transactional
    public void registerUser(RegisterRequest r) {

        /* ---------- Vérifications de base ---------- */
        if (userRepo.existsByEmail(r.getEmail()))
            throw new IllegalArgumentException("Email already in use");

        if (r.getPassword() == null || r.getPassword().length() < 6)
            throw new IllegalArgumentException("Password trop court (>=6)");

        /* ---------- Création entité ---------- */
        Utilisateur u = new Utilisateur();

        /* ---------- Champs communs ---------- */
        u.setNom(r.getNom());
        u.setPrenom(r.getPrenom());
        u.setEmail(r.getEmail());
        u.setMotDePasse(passwordEncoder.encode(r.getPassword()));
        u.setTypeUtilisateur(r.getTypeUtilisateur());
        u.setDateCreation(LocalDateTime.now());
        u.setDerniereMiseAJour(LocalDateTime.now());
        u.setEstActif(true);

        /* ---------- Init compteurs/solde ---------- */
        u.setSoldeEscrow(BigDecimal.ZERO);
        u.setNombreSwipes(0);
        u.setLikesRecus(0);
        u.setMatchesObtenus(0);

        /* ---------- Contact & profil ---------- */
        u.setNumeroTelephone(r.getNumeroTelephone());
        u.setPhotoProfilUrl(r.getPhotoProfilUrl());
        u.setLanguePref(r.getLanguePref());
        u.setLocalisation(r.getLocalisation());

        /* ================================================================
                      FREELANCE
           ================================================================ */
        if (r.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.FREELANCE) {
            u.setCompetences(safeSet(r.getCompetences()));
            u.setTarifHoraire(r.getTarifHoraire());
            u.setTarifJournalier(r.getTarifJournalier());
            u.setDisponibilite(r.getDisponibilite());
            u.setBio(r.getBio());
            u.setNiveauExperience(r.getNiveauExperience());
            u.setCategories(safeSetCategorie(r.getCategories()));
            u.setPortfolioUrls(safeList(r.getPortfolioUrls()));
        }

        /* ================================================================
                         CLIENT  (avec sous-type)
           ================================================================ */
        if (r.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.CLIENT) {

            /* Sous-type obligatoire pour un client */
            if (r.getTypeClient() == null)
                throw new IllegalArgumentException("typeClient requis pour un client");

            u.setTypeClient(r.getTypeClient());

            switch (r.getTypeClient()) {

                /* ---------- PME / STARTUP  ou  CLIENT ÉTRANGER ---------- */
                case PME_STARTUP:
                case CLIENT_ETRANGER:
                    if (r.getNomEntreprise() == null || r.getNomEntreprise().isBlank())
                        throw new IllegalArgumentException("Le nom de l'entreprise est obligatoire pour ce type de client.");
                    u.setNomEntreprise(r.getNomEntreprise());
                    u.setSiteEntreprise(r.getSiteEntreprise());
                    u.setDescriptionEntreprise(r.getDescriptionEntreprise());
                    break;

                /* ---------- ENTREPRENEUR INDIVIDUEL ---------- */
                case ENTREPRENEUR:
                    /* Pas de champ nomEntreprise obligatoire, mais on peut stocker un site/description perso */
                    u.setNomEntreprise(null);
                    u.setSiteEntreprise(r.getSiteEntreprise());
                    u.setDescriptionEntreprise(r.getDescriptionEntreprise());
                    break;

                /* ---------- ÉTUDIANT / PARTICULIER ---------- */
                case ETUDIANT_PARTICULIER:
                    /* Aucun champ société : on force à null pour éviter la confusion */
                    u.setNomEntreprise(null);
                    u.setSiteEntreprise(null);
                    u.setDescriptionEntreprise(null);
                    break;

                default:
                    throw new IllegalStateException("Sous-type client non géré : " + r.getTypeClient());
            }
        }

        /* ---------- Push tokens (commun) ---------- */
        if (r.getPushTokens() != null && !r.getPushTokens().isEmpty()) {
            u.setPushTokens(new HashSet<>(r.getPushTokens()));
        }

        /* ---------- Normalisation ---------- */
        normalizeCollections(u);

        userRepo.save(u);
    }

    /* =================================================================
       Login
       ================================================================= */
//    public TokenPair login(LoginRequest r) {
//        Authentication auth = authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(r.getEmail(), r.getPassword())
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        userRepo.findByEmail(r.getEmail()).ifPresent(u -> {
//            u.setDateDerniereConnexion(LocalDateTime.now());
//            userRepo.save(u);
//        });
//
//        return jwtService.generateTokenPair(auth);
//    }
//
//    /* =================================================================
//       Refresh
//       ================================================================= */
//    public TokenPair refreshToken(RefeshTokenRequest req) {
//
//        String refresh = req.getRefreshToken();
//        if (!jwtService.isRefreshToken(refresh))
//            throw new IllegalArgumentException("Invalid refresh token");
//
//        String email   = jwtService.extractUsername(refresh);
//        UserDetails u  = userDetailsService.loadUserByUsername(email);
//
//        UsernamePasswordAuthenticationToken auth =
//            new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
//
//        String newAccess = jwtService.generateAccessToken(auth);
//        return new TokenPair(newAccess, refresh);
//    }
    
	public TokenPair login(LoginRequest r) {
	    	
	    	System.out.println("hello from my home out out");
	    	
	        Authentication auth = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(r.getEmail(), r.getPassword())
	        );
	        SecurityContextHolder.getContext().setAuthentication(auth);
	        
	        System.out.println("hello from my home out");
	
	//        userRepo.findByEmail(r.getEmail()).ifPresent(u -> {
	//            u.setDateDerniereConnexion(LocalDateTime.now());
	//            userRepo.save(u);
	//        });
	        
	        Optional<Utilisateur> u = userRepo.findByEmail(r.getEmail());
	        if(u == null || u.isEmpty() || u.get().getAuthProvider() == AuthProvider.GOOGLE) {
	        	System.out.println("hello from my home");
	        	throw new RuntimeErrorException(null, "User not found");
	        }
	        
	
	        
	        Utilisateur user = userRepo.findByEmail(r.getEmail())
	        	    .orElseThrow(() -> new RuntimeException("User not found"));
	        
	        TokenPair tokenPair = jwtService.generateTokenPair(auth);
	        String rawRereshToken = refreshTokenService.createAndStoreRefreshToken(user.getId());
	        TokenPair tp = new TokenPair(tokenPair.getAccessToken() , rawRereshToken);
	        
	        
	
	        return tp;
	    }

	public TokenPair refreshToken(RefeshTokenRequest req) {
		RefreshToken token = refreshTokenService.validateRefreshToken(req.getRefreshToken());
		Utilisateur user = token.getUser();
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
		
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null , userDetails.getAuthorities());
		
		String newAccessToken = jwtService.generateAccessToken(auth);
		
		return new TokenPair(newAccessToken , req.getRefreshToken());
	}

    /* =================================================================
       Helpers internes
       ================================================================= */
    private Set<String> safeSet(List<String> list) {
        return list == null ? new HashSet<>() : new HashSet<>(list);
    }

    private Set<Categorie> safeSetCategorie(Collection<Categorie> c) {
        return c == null ? new HashSet<>() : new HashSet<>(c);
    }

    private List<String> safeList(List<String> l) {
        return l == null ? new ArrayList<>() : l;
    }

    private void normalizeCollections(Utilisateur u) {
        if (u.getCompetences()   == null) u.setCompetences(new HashSet<>());
        if (u.getListeBadges()   == null) u.setListeBadges(new HashSet<>());
        if (u.getPortfolioUrls() == null) u.setPortfolioUrls(new ArrayList<>());
        if (u.getCategories()    == null) u.setCategories(new HashSet<>());
        if (u.getPushTokens()    == null) u.setPushTokens(new HashSet<>());
    }
}
