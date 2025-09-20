package com.projet.freelencetinder.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "chat_message",
    indexes = {
        @Index(name = "idx_msg_conv_sent", columnList = "conversation_id,sentAt"),
        @Index(name = "idx_msg_unread", columnList = "conversation_id,receiver_id,seen")
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Relations */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Utilisateur sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Utilisateur receiver;

    /* Contenu */
    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type = MessageType.TEXT;

    private String fileUrl;
    private String fileType;

    /* Statut & audit */
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean seen = false;

    /* Constructeurs */
    public ChatMessage() {}

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }

    /* ===================== Getters / Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public Utilisateur getSender() { return sender; }
    public void setSender(Utilisateur sender) { this.sender = sender; }

    public Utilisateur getReceiver() { return receiver; }
    public void setReceiver(Utilisateur receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    /* Enum */
    public enum MessageType { TEXT, LINK, FILE }
}
