package com.example.SpringBootAssignment1.repository.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserTableCreator {

    private final JdbcTemplate jdbcTemplate;

    public UserTableCreator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTables() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS myUser (" +
                        "id UUID DEFAULT uuid_generate_v4(), " +
                        "name VARCHAR(255), " +
                        "gender VARCHAR(255), " +
                        "mobileNumber VARCHAR(255), " +
                        "address JSON, " +
                        "active BOOLEAN, " +
                        "createdTime VARCHAR(255), " +
                        "PRIMARY KEY (id, active) " +
                        ") PARTITION BY LIST (active);"
        );

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS activeUser PARTITION OF myUser FOR VALUES IN (TRUE);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS inActiveUser PARTITION OF myUser FOR VALUES IN (FALSE);");
    }
}
