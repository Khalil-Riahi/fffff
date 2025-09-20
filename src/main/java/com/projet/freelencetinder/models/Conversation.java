package com.projet.freelencetinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "conversation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"mission_id", "client_id", "freelance_id"}),
    indexes = {
        @Index(name = "idx_conv_last_message_at", columnList = "lastMessageAt"),
        @Index(name = "idx_conv_client", columnList = "client_id"),
        @Index(name = "idx_conv_freelance", columnList = "freelance_id")
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optimistic locking pour éviter les écrasements concurrents. */
    @Version
    private Long version;

    /* Participants & mission */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_id", nullable = false)
    private Utilisateur freelance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    /* Audit */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* Dernier message (snapshot pour performance) */
    @Column
    private LocalDateTime lastMessageAt;

    @Column(length = 4000)
    private String lastMessageContent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ChatMessage.MessageType lastMessageType;

    /* Constructeurs */
    public Conversation() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /* ===================== Getters / Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public Utilisateur getFreelance() { return freelance; }
    public void setFreelance(Utilisateur freelance) { this.freelance = freelance; }

    public Mission getMission() { return mission; }
    public void setMission(Mission mission) { this.mission = mission; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public String getLastMessageContent() { return lastMessageContent; }
    public void setLastMessageContent(String lastMessageContent) { this.lastMessageContent = lastMessageContent; }

    public ChatMessage.MessageType getLastMessageType() { return lastMessageType; }
    public void setLastMessageType(ChatMessage.MessageType lastMessageType) { this.lastMessageType = lastMessageType; }
}
