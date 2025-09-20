package com.projet.freelencetinder.servcie;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.models.RefreshToken;
import com.projet.freelencetinder.models.Utilisateur;
import com.projet.freelencetinder.repository.RefreshTokenRepository;
import com.projet.freelencetinder.repository.UtilisateurRepository;
import org.springframework.transaction.annotation.Transactional;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	@Autowired
    private RefreshTokenRepository refreshTokenRepository;
	
	@Autowired
    private UtilisateurRepository utilisateurRepository;

    @Value("${security.refresh-token.expiration-ms}")
    private Long refreshTokenDurationMs;

    public String createAndStoreRefreshToken(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = DigestUtils.sha256Hex(rawToken); // You can use BCrypt too

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setHashedToken(hashedToken);
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        refreshTokenRepository.save(token);

        return rawToken; // return un-hashed version to store in cookie
    }

    
    public RefreshToken validateRefreshToken(String rawToken) {
        String hashed = DigestUtils.sha256Hex(rawToken);

        RefreshToken token = refreshTokenRepository.findByHashedToken(hashed)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    @Transactional
    public void revokeByUser(String rawToken) {
    	String hashedtoken = DigestUtils.sha256Hex(rawToken);
        refreshTokenRepository.deleteByHashedToken(hashedtoken);
    }
}
