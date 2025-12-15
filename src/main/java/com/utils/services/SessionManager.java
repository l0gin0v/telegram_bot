package com.utils.services;

import com.utils.dao.UserSessionDAO;
import com.utils.models.UserSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final UserSessionDAO sessionDAO;
    private final Map<Long, UserSession> cache;

    public SessionManager() {
        this.sessionDAO = new UserSessionDAO();
        this.cache = new ConcurrentHashMap<>();

        sessionDAO.createTableIfNotExists();

        startCleanupTask();
    }

    public void createOrUpdateSession(Long userId, String city, String state, boolean isActive) {
        UserSession session = new UserSession(userId, city, state, isActive);

        cache.put(userId, session);

        new Thread(() -> sessionDAO.saveOrUpdate(session)).start();
    }

    public Optional<UserSession> getSession(Long userId) {
        if (cache.containsKey(userId)) {
            return Optional.of(cache.get(userId));
        }

        Optional<UserSession> session = sessionDAO.findById(userId);
        session.ifPresent(s -> cache.put(userId, s));

        return session;
    }

    public boolean isSessionActive(Long userId) {
        return getSession(userId)
                .map(UserSession::isActive)
                .orElse(false);
    }

    public void updateCity(Long userId, String city) {
        getSession(userId).ifPresent(session -> {
            session.setCity(city);
            sessionDAO.saveOrUpdate(session);
            cache.put(userId, session);
        });
    }

    public void updateState(Long userId, String state) {
        getSession(userId).ifPresent(session -> {
            session.setState(state);
            session.setLastActivity(java.time.LocalDateTime.now());
            sessionDAO.saveOrUpdate(session);
            cache.put(userId, session);
        });
    }

    public void activateSession(Long userId, String city) {
        createOrUpdateSession(userId, city, "DEFAULT", true);
    }

    public void deactivateSession(Long userId) {
        sessionDAO.deactivateSession(userId);
        cache.remove(userId);
    }

    public void updateActivity(Long userId) {
        sessionDAO.updateActivity(userId);
        getSession(userId).ifPresent(session -> {
            session.setLastActivity(java.time.LocalDateTime.now());
            cache.put(userId, session);
        });
    }

    private void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    sessionDAO.cleanupOldSessions(7);

                    Thread.sleep(24 * 60 * 60 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public List<UserSession> getAllActiveSessions() {
        return sessionDAO.getActiveSessions();
    }
}