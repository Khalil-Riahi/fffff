package com.projet.freelencetinder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.freelencetinder.models.RefreshToken;
import com.projet.freelencetinder.models.Utilisateur;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByHashedToken(String hashedToken);
    void deleteByUser(Utilisateur user);
    void deleteByHashedToken(String hashedToken);
}
