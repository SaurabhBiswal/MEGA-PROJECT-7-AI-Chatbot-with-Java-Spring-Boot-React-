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
            String dbName = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
            log.info("Detected database: {}", dbName);
            boolean isPostgres = dbName.toLowerCase().contains("postgresql");

            log.info("Starting automated database initialization...");

            if (isPostgres) {
                // 0. Disable timeout for this session
                try {
                    jdbcTemplate.execute("SET statement_timeout = 0");
                    log.info("✅ Statement timeout disabled for initialization session.");
                } catch (Exception e) {
                    log.warn("Could not set statement timeout: {}", e.getMessage());
                }

                // 1. Enable pgvector
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                log.info("✅ pgvector extension ensured.");
            }

            // 2. Create Users table
            String createUsersSql = isPostgres
                    ? "CREATE TABLE IF NOT EXISTS users (id BIGSERIAL PRIMARY KEY, username VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(50) DEFAULT 'ROLE_USER')"
                    : "CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, role VARCHAR(50) DEFAULT 'ROLE_USER')";
            jdbcTemplate.execute(createUsersSql);
            log.info("✅ Users table ensured.");

            // 3. Create Conversations table
            String createConvSql = isPostgres
                    ? "CREATE TABLE IF NOT EXISTS conversations (id BIGSERIAL PRIMARY KEY, user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, title VARCHAR(255), started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                    : "CREATE TABLE IF NOT EXISTS conversations (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id BIGINT REFERENCES users(id) ON DELETE CASCADE, title VARCHAR(255), started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            jdbcTemplate.execute(createConvSql);
            log.info("✅ Conversations table ensured.");

            // 4. Create Messages table
            String createMsgSql = isPostgres
                    ? "CREATE TABLE IF NOT EXISTS messages (id BIGSERIAL PRIMARY KEY, conversation_id BIGINT REFERENCES conversations(id) ON DELETE CASCADE, content TEXT NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, type VARCHAR(50) NOT NULL, status VARCHAR(50) DEFAULT 'SENT', attachment_url TEXT, attachment_type VARCHAR(100), sender_id BIGINT REFERENCES users(id) ON DELETE SET NULL)"
                    : "CREATE TABLE IF NOT EXISTS messages (id BIGINT AUTO_INCREMENT PRIMARY KEY, conversation_id BIGINT REFERENCES conversations(id) ON DELETE CASCADE, content TEXT NOT NULL, timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, type VARCHAR(50) NOT NULL, status VARCHAR(50) DEFAULT 'SENT', attachment_url TEXT, attachment_type VARCHAR(100), sender_id BIGINT REFERENCES users(id) ON DELETE SET NULL)";
            jdbcTemplate.execute(createMsgSql);
            log.info("✅ Messages table ensured.");

            // 5. Create Knowledge table
            if (isPostgres) {
                jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS knowledge_base (id BIGSERIAL PRIMARY KEY, content TEXT NOT NULL, embedding vector(384), file_name VARCHAR(255) NOT NULL)");
                log.info("✅ Knowledge base table ensured.");
            } else {
                jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS knowledge_base (id BIGINT AUTO_INCREMENT PRIMARY KEY, content TEXT NOT NULL, file_name VARCHAR(255) NOT NULL)");
                log.info("✅ Knowledge base table ensured (without vector column).");
            }

            log.info("🚀 Database initialization completed successfully!");
        } catch (Exception e) {
            log.error("❌ Database initialization FAILED: {}", e.getMessage());
        }
    }
}
