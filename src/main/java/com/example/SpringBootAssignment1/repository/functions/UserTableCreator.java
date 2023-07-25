package com.example.SpringBootAssignment1.repository.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserTableCreator {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void createTables() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS myUser (" +
                        "id VARCHAR(255), " +
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
