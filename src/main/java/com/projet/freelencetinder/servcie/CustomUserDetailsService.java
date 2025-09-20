package com.projet.freelencetinder.servcie;


import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.UtilisateurRepository;

@Service               // <-- expose automatiquement le bean
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository repo;

    public CustomUserDetailsService(UtilisateurRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.builder()
                .username(u.getEmail())
                .password(u.getMotDePasse())               // hashé Bcrypt
                .authorities(u.getTypeUtilisateur().name())// FREELANCE / CLIENT / ADMIN
                .disabled(!u.isEstActif())
                .build();
    }
}
