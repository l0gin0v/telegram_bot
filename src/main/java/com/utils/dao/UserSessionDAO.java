package com.utils.dao;

import com.utils.config.DatabaseConfig;
import com.utils.models.UserSession;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserSessionDAO {

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_sessions (
                user_id BIGINT PRIMARY KEY,
                city VARCHAR(100),
                state VARCHAR(50),
                is_active BOOLEAN DEFAULT TRUE,
                last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_is_active (is_active),
                INDEX idx_last_activity (last_activity)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Таблица user_sessions создана/проверена");
        } catch (SQLException e) {
            System.err.println("❌ Ошибка при создании таблицы: " + e.getMessage());
        }
    }

    public void saveOrUpdate(UserSession session) {
        String sql = """
            INSERT INTO user_sessions (user_id, city, state, is_active, last_activity) 
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                city = VALUES(city),
                state = VALUES(state),
                is_active = VALUES(is_active),
                last_activity = VALUES(last_activity)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, session.getUserId());
            pstmt.setString(2, session.getCity());
            pstmt.setString(3, session.getState());
            pstmt.setBoolean(4, session.isActive());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при сохранении сессии: " + e.getMessage());
        }
    }

    public Optional<UserSession> findById(Long userId) {
        String sql = "SELECT * FROM user_sessions WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                UserSession session = new UserSession();
                session.setUserId(rs.getLong("user_id"));
                session.setCity(rs.getString("city"));
                session.setState(rs.getString("state"));
                session.setActive(rs.getBoolean("is_active"));
                session.setLastActivity(rs.getTimestamp("last_activity").toLocalDateTime());
                session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                return Optional.of(session);
            }

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при получении сессии: " + e.getMessage());
        }

        return Optional.empty();
    }

    public void updateActivity(Long userId) {
        String sql = "UPDATE user_sessions SET last_activity = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при обновлении активности: " + e.getMessage());
        }
    }

    public void deactivateSession(Long userId) {
        String sql = "UPDATE user_sessions SET is_active = FALSE WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при завершении сессии: " + e.getMessage());
        }
    }

    public void cleanupOldSessions(int daysOld) {
        String sql = "DELETE FROM user_sessions WHERE is_active = FALSE AND last_activity < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, daysOld);
            int deleted = pstmt.executeUpdate();
            System.out.println("🧹 Удалено старых сессий: " + deleted);

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при очистке сессий: " + e.getMessage());
        }
    }


    public List<UserSession> getActiveSessions() {
        List<UserSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM user_sessions WHERE is_active = TRUE ORDER BY last_activity DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserSession session = new UserSession();
                session.setUserId(rs.getLong("user_id"));
                session.setCity(rs.getString("city"));
                session.setState(rs.getString("state"));
                session.setActive(rs.getBoolean("is_active"));
                session.setLastActivity(rs.getTimestamp("last_activity").toLocalDateTime());
                session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                sessions.add(session);
            }

        } catch (SQLException e) {
            System.err.println("❌ Ошибка при получении активных сессий: " + e.getMessage());
        }

        return sessions;
    }
}