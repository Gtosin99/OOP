package com.sante.lims.model;

import java.time.LocalDateTime;

public class NotificationItem {
    private long id;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public NotificationItem(long id, String subject, String message, LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.subject = subject;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
    }

    public long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }
}
