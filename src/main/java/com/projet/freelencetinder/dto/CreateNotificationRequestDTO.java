package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;
import java.util.Map;
import com.projet.freelencetinder.models.Notification.*;

public class CreateNotificationRequestDTO {

    private Long recipientId;
    private Long senderId;                 // peut être null
    private NotificationType type;
    private Long referenceId;
    private String title;
    private String body;
    private Priority priority  = Priority.NORMAL;
    private Channel channel    = Channel.WEB;
    private LocalDateTime expiresAt;       // optionnel
    private Map<String, Object> data;

    /* Getters / Setters */
    public Long getRecipientId()                 { return recipientId; }
    public void setRecipientId(Long r)           { this.recipientId = r; }
    public Long getSenderId()                    { return senderId; }
    public void setSenderId(Long s)              { this.senderId = s; }
    public NotificationType getType()            { return type; }
    public void setType(NotificationType t)      { this.type = t; }
    public Long getReferenceId()                 { return referenceId; }
    public void setReferenceId(Long ref)         { this.referenceId = ref; }
    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }
    public String getBody()                      { return body; }
    public void setBody(String body)             { this.body = body; }
    public Priority getPriority()                { return priority; }
    public void setPriority(Priority p)          { this.priority = p; }
    public Channel getChannel()                  { return channel; }
    public void setChannel(Channel c)            { this.channel = c; }
    public LocalDateTime getExpiresAt()          { return expiresAt; }
    public void setExpiresAt(LocalDateTime e)    { this.expiresAt = e; }
    public Map<String, Object> getData()         { return data; }
    public void setData(Map<String, Object> d)   { this.data = d; }
}
