package com.projet.freelencetinder.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hashedToken; // hashed UUID (SHA-256 or bcrypt)

    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur user;

    @Column(nullable = false)
    private Instant expiryDate;
    
    public RefreshToken() {
    	
    }

	public RefreshToken(Long id, String hashedToken, Utilisateur user, Instant expiryDate) {
		super();
		this.id = id;
		this.hashedToken = hashedToken;
		this.user = user;
		this.expiryDate = expiryDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHashedToken() {
		return hashedToken;
	}

	public void setHashedToken(String hashedToken) {
		this.hashedToken = hashedToken;
	}

	public Utilisateur getUser() {
		return user;
	}

	public void setUser(Utilisateur user) {
		this.user = user;
	}

	public Instant getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}

    
    // Getters and Setters
}
