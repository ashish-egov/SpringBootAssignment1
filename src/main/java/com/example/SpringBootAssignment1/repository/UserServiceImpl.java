package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createTable() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS myUser (" +
                        "id SERIAL, " +
                        "name VARCHAR(255), " +
                        "gender VARCHAR(255), " +
                        "mobileNumber VARCHAR(255), " +
                        "address JSON, " +
                        "active BOOLEAN, " +
                        "createdTime BIGINT, " +
                        "PRIMARY KEY (id, active), " +
                        "CONSTRAINT uniqueNameAndMobileNumber UNIQUE (name, mobileNumber, active)" +
                        ") PARTITION BY LIST (active);"
        );
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS activeUser PARTITION OF myUser FOR VALUES IN (TRUE);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS inActiveUser PARTITION OF myUser FOR VALUES IN (FALSE);");
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM myUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public List<User> getActiveUsers() {
        String sql = "SELECT * FROM activeUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public List<User> getInActiveUsers() {
        String sql = "SELECT * FROM inActiveUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    public boolean isUserValid(User user) {
        String sql = "SELECT COUNT(*) FROM myUser WHERE name=? AND mobileNumber=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, user.getName(), user.getMobileNumber());
        return count == null || count == 0;
    }
    @Override
    public ResponseEntity<?> createUser(List<User> userList) {
        try {
            List<User> createdUserList = new UserCreator(jdbcTemplate).createUsers(userList);
            return ResponseEntity.ok(createdUserList);
        } catch (UserCreationException e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("createdUsers", e.getCreatedUsers());
            responseBody.put("duplicateUsers", e.getDuplicateUsers());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
        }
    }

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        String sql = "SELECT * FROM myUser";
        List<Object> args = new ArrayList<>();
        boolean hasCriteria = false;

        if (criteria.getId() != null) {
            sql += " WHERE id = ?";
            args.add(criteria.getId());
            hasCriteria = true;
        }

        if (criteria.getMobileNumber() != null) {
            if (hasCriteria) {
                sql += " AND mobileNumber = ?";
            } else {
                sql += " WHERE mobileNumber = ?";
                hasCriteria = true;
            }
            args.add(criteria.getMobileNumber());
        }

        if (criteria.getActive() != null) {
            if (hasCriteria) {
                sql += " AND active = ?";
            } else {
                sql += " WHERE active = ?";
                hasCriteria = true;
            }
            args.add(criteria.getActive());
        }

        return jdbcTemplate.query(sql, args.toArray(), new UserRowMapper());
    }


    public User getUserById(Long id) {
        String sql = "SELECT * FROM myUser WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @Override
    public List<User> updateUser(List<User> userList) {
        List<User> updatedUserList = new ArrayList<>();
        for (User user : userList) {
            Long id = user.getId();
            User existingUser = getUserById(id);
            if (existingUser == null) {
                continue;
            }
            String name = user.getName() != null ? user.getName() : existingUser.getName();
            String gender = user.getGender() != null ? user.getGender() : existingUser.getGender();
            String mobileNumber = user.getMobileNumber() != null ? user.getMobileNumber() : existingUser.getMobileNumber();
            Address address = user.getAddress() != null ? user.getAddress() : existingUser.getAddress();
            boolean active = user.isActive() != null ? user.isActive() : existingUser.isActive();
            Long createdTime = existingUser.getCreatedTime();
            existingUser.setId(id);
            existingUser.setName(name);
            existingUser.setGender(gender);
            existingUser.setMobileNumber(mobileNumber);
            existingUser.setAddress(address);
            existingUser.setActive(active);
            existingUser.setCreatedTime(createdTime);
            ObjectMapper objectMapper = new ObjectMapper();
            String addressJson;
            try {
                addressJson = objectMapper.writeValueAsString(address);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing address object to JSON", e);
            }
            String sql = "UPDATE myUser SET name=?, gender=?, mobileNumber=?, address=?::json, active=?, createdTime=? WHERE id=?";
            jdbcTemplate.update(sql, name, gender, mobileNumber, addressJson, active, createdTime, id);
            updatedUserList.add(existingUser);
        }
        return updatedUserList;
    }

    @Override
    public String deleteUser(Long id) {
        String sql = "DELETE FROM myUser WHERE id=?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            return "No user exists with id " + id;
        }
        return "User of id " + id + " has been deleted.";
    }
}