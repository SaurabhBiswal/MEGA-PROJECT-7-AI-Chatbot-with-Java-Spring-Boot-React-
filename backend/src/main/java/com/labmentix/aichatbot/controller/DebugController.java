package com.labmentix.aichatbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@Slf4j
public class DebugController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-status")
    public Map<String, Object> getDbStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Check tables
            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
                    String.class);
            status.put("tables", tables);

            // Check columns in messages
            if (tables.contains("messages")) {
                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                        "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'messages'");
                status.put("messages_columns", columns);
            }

            // Check columns in users
            if (tables.contains("users")) {
                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                        "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users'");
                status.put("users_columns", columns);
            }

            status.put("database", "CONNECTED");
        } catch (Exception e) {
            status.put("error", e.getMessage());
            log.error("Debug stats failed", e);
        }
        return status;
    }
}
