package com.projet.freelencetinder.models;

import java.time.LocalDateTime;
import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;

@Entity
@Table(
    name = "notification",
    indexes = {
        @Index(name = "idx_notif_recipient_seen", columnList = "recipient_id,seen"),
        @Index(name = "idx_notif_created_at",     columnList = "createdAt"),
        @Index(name = "idx_notif_type",           columnList = "type"),
        @Index(name = "idx_notif_archived",       columnList = "archived")
    }
)
public class Notification {

    /* ===== Identité ===== */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /* ===== Relations ===== */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Utilisateur recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Utilisateur sender;               // peut être null (système)

    /* ===== Métadonnées ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    private Long referenceId;                 // id ressource liée

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 600)
    private String body;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;         // payload libre

    /* ===== Attributs ajoutés ===== */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Channel channel = Channel.WEB;

    @Column(nullable = false)
    private boolean archived = false;         // masquée dans “ancienne”

    private LocalDateTime expiresAt;          // TTL facultatif

    /* ===== Statut & audit ===== */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean seen      = false;

    @Column(nullable = false)
    private boolean delivered = false;

    /* ===== Hooks ===== */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /* ===== Enums ===== */
    public enum NotificationType {
        /* CLIENT */
        FREELANCE_INTERESSE_MISSION,
        NOUVEAU_MESSAGE_FREELANCE,
        LIVRABLE_RECU,
        MISSION_TERMINEE,
        /* FREELANCE */
        SELECTIONNE_POUR_MISSION,
        NOUVEAU_MESSAGE_CLIENT,
        DEADLINE_PROCHE,
        MISSION_ATTRIBUEE
    }
    public enum Priority { LOW, NORMAL, HIGH }
    public enum Channel  { WEB, PUSH, EMAIL }

    /* ===== Getters/Setters ===== */
    public Long getId()                           { return id; }
    public Long getVersion()                      { return version; }

    public Utilisateur getRecipient()             { return recipient; }
    public void setRecipient(Utilisateur r)       { this.recipient = r; }

    public Utilisateur getSender()                { return sender; }
    public void setSender(Utilisateur s)          { this.sender = s; }

    public NotificationType getType()             { return type; }
    public void setType(NotificationType t)       { this.type = t; }

    public Long getReferenceId()                  { return referenceId; }
    public void setReferenceId(Long refId)        { this.referenceId = refId; }

    public String getTitle()                      { return title; }
    public void setTitle(String title)            { this.title = title; }

    public String getBody()                       { return body; }
    public void setBody(String body)              { this.body = body; }

    public Map<String, Object> getData()          { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Priority getPriority()                 { return priority; }
    public void setPriority(Priority priority)    { this.priority = priority; }

    public Channel getChannel()                   { return channel; }
    public void setChannel(Channel channel)       { this.channel = channel; }

    public boolean isArchived()                   { return archived; }
    public void setArchived(boolean archived)     { this.archived = archived; }

    public LocalDateTime getExpiresAt()           { return expiresAt; }
    public void setExpiresAt(LocalDateTime ea)    { this.expiresAt = ea; }

    public LocalDateTime getCreatedAt()           { return createdAt; }

    public boolean isSeen()                       { return seen; }
    public void setSeen(boolean seen)             { this.seen = seen; }

    public boolean isDelivered()                  { return delivered; }
    public void setDelivered(boolean d)           { this.delivered = d; }
}
