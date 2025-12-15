package com.utils.models;

import java.time.LocalDateTime;

public class UserSession {
    private Long userId;
    private String city;
    private String state;
    private boolean isActive;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;


    public UserSession() {}

    public UserSession(Long userId, String city, String state, boolean isActive) {
        this.userId = userId;
        this.city = city;
        this.state = state;
        this.isActive = isActive;
        this.lastActivity = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}