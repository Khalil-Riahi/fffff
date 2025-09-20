package com.projet.freelencetinder.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "withdrawal_method",
       uniqueConstraints = @UniqueConstraint(name = "uk_withdrawal_primary", columnNames = {"freelance_id","principal"}))
public class WithdrawalMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    public enum Type { RIB, D17 }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    /* ---------- Détails selon type ---------- */
    @Size(max = 30)
    private String rib;

    @Size(max = 20)
    private String walletNumber;

    @Column(nullable = false)
    private boolean principal = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    /* ---------- Relations ---------- */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freelance_id", nullable = false)
    @JsonIgnore
    private Utilisateur freelance;

    /* ---------- Hooks ---------- */
    @PrePersist
    protected void onCreate() {
        this.dateAjout = LocalDateTime.now();
    }

    /* ---------- Getters / Setters ---------- */
    public Long getId() { return id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public String getWalletNumber() { return walletNumber; }
    public void setWalletNumber(String walletNumber) { this.walletNumber = walletNumber; }

    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }

    public LocalDateTime getDateAjout() { return dateAjout; }

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }
}