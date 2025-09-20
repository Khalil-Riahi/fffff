package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;
import java.util.Map;
import com.projet.freelencetinder.models.Notification.*;

public class NotificationSocketDTO {

    private Long id;
    private NotificationType type;
    private String title;
    private String body;
    private Priority priority;
    private Channel channel;
    private Long referenceId;
    private LocalDateTime createdAt;
    private Map<String, Object> data;

    /* Getters / Setters */
    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }

    public NotificationType getType()            { return type; }
    public void setType(NotificationType t)      { this.type = t; }

    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }

    public String getBody()                      { return body; }
    public void setBody(String body)             { this.body = body; }

    public Priority getPriority()                { return priority; }
    public void setPriority(Priority p)          { this.priority = p; }

    public Channel getChannel()                  { return channel; }
    public void setChannel(Channel c)            { this.channel = c; }

    public Long getReferenceId()                 { return referenceId; }
    public void setReferenceId(Long r)           { this.referenceId = r; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime c)    { this.createdAt = c; }

    public Map<String, Object> getData()         { return data; }
    public void setData(Map<String, Object> d)   { this.data = d; }
}
