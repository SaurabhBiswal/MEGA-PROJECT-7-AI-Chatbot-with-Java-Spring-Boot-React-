package com.labmentix.aichatbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("Starting automated database initialization...");

            // 1. Enable pgvector
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("✅ pgvector extension ensured.");

            // 2. Create Users table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "username VARCHAR(255) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) DEFAULT 'ROLE_USER')");
            log.info("✅ Users table ensured.");

            // 3. Create Conversations table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS conversations (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, " +
                    "title VARCHAR(255), " +
                    "started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            log.info("✅ Conversations table ensured.");

            // 4. Create Messages table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "conversation_id BIGINT REFERENCES conversations(id) ON DELETE CASCADE, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "type VARCHAR(50) NOT NULL)");
            log.info("✅ Messages table ensured.");

            // 5. FIX: Add missing columns to Messages individually to avoid failures
            String[] alterCommands = {
                    "ALTER TABLE messages ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'SENT'",
                    "ALTER TABLE messages ADD COLUMN IF NOT EXISTS attachment_url TEXT",
                    "ALTER TABLE messages ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(100)",
                    "ALTER TABLE messages ADD COLUMN IF NOT EXISTS sender_id BIGINT REFERENCES users(id) ON DELETE SET NULL"
            };

            for (String cmd : alterCommands) {
                try {
                    jdbcTemplate.execute(cmd);
                    log.info("✅ Executed: {}", cmd);
                } catch (Exception e) {
                    log.warn("⚠️ Command failed (might already exist): {} - Error: {}", cmd, e.getMessage());
                }
            }

            // 6. Create Knowledge table
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS knowledge_base (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "content TEXT NOT NULL, " +
                    "embedding vector(384), " +
                    "file_name VARCHAR(255) NOT NULL)");
            log.info("✅ Knowledge base table ensured.");

            log.info("🚀 Database initialization completed successfully!");
        } catch (Exception e) {
            log.error("❌ Database initialization FAILED: {}", e.getMessage());
        }
    }
}
