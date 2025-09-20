package com.projet.freelencetinder.dto;
import java.time.LocalDateTime;
import java.util.Map;
import com.projet.freelencetinder.models.Notification.*;

public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String title;
    private String body;
    private boolean seen;
    private boolean archived;
    private Priority priority;
    private Channel channel;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long referenceId;
    private Map<String, Object> data;

    /* Getters / Setters */
    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }

    public NotificationType getType()         { return type; }
    public void setType(NotificationType t)   { this.type = t; }

    public String getTitle()                  { return title; }
    public void setTitle(String title)        { this.title = title; }

    public String getBody()                   { return body; }
    public void setBody(String body)          { this.body = body; }

    public boolean isSeen()                   { return seen; }
    public void setSeen(boolean seen)         { this.seen = seen; }

    public boolean isArchived()               { return archived; }
    public void setArchived(boolean arch)     { this.archived = arch; }

    public Priority getPriority()             { return priority; }
    public void setPriority(Priority p)       { this.priority = p; }

    public Channel getChannel()               { return channel; }
    public void setChannel(Channel c)         { this.channel = c; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }

    public LocalDateTime getExpiresAt()       { return expiresAt; }
    public void setExpiresAt(LocalDateTime e) { this.expiresAt = e; }

    public Long getReferenceId()              { return referenceId; }
    public void setReferenceId(Long ref)      { this.referenceId = ref; }

    public Map<String, Object> getData()      { return data; }
    public void setData(Map<String, Object> d){ this.data = d; }
}
