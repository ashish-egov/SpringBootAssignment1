package com.example.SpringBootAssignment1.repository.functions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class UserDeleter {
    private final JdbcTemplate jdbcTemplate;

    public UserDeleter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String deleteUser(UUID id) {
        String sql = "DELETE FROM myUser WHERE id=?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            return "No user exists with id " + id;
        }
        return "User of id " + id + " has been deleted.";
    }
}

