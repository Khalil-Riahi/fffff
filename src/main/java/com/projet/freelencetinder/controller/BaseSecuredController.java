package com.projet.freelencetinder.controller;

import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseSecuredController {

    protected final UtilisateurRepository userRepo;

    protected BaseSecuredController(UtilisateurRepository userRepo) {
        this.userRepo = userRepo;
    }

    protected Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        String identifier = auth.getName(); // ici = email
        Utilisateur u = userRepo.findByEmail(identifier)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable: " + identifier));
        return u.getId();
    }
}