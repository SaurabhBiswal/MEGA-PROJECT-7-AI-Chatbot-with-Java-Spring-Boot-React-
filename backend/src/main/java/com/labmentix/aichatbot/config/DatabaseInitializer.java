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
            log.info("Attempting to enable pgvector extension...");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("pgvector extension enabled successfully!");
        } catch (Exception e) {
            log.warn("Could not enable pgvector extension automatically. " +
                    "If you are using PostgreSQL, please run 'CREATE EXTENSION vector' manually in your SQL editor. " +
                    "Error: {}", e.getMessage());
        }
    }
}
