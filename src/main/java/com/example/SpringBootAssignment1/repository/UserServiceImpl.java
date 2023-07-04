package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final JdbcTemplate jdbcTemplate;

    private UserTableCreator userTableCreator;

    public UserServiceImpl(JdbcTemplate jdbcTemplate,UserTableCreator userTableCreator) {
        this.jdbcTemplate = jdbcTemplate;
        this.userTableCreator=userTableCreator;
    }

    @PostConstruct
    public void createTable() {
        userTableCreator.createTables();
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
    public String createUser(List<User> userList) {
        int createdUserCount = 0;
        int duplicateUserCount = 0;

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = formatter.format(now);

        for (User user : userList) {
            // Check if user already exists
            String sql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ?";
            int count = jdbcTemplate.queryForObject(sql, Integer.class, user.getName(), user.getMobileNumber());

            if (count > 0) {
                duplicateUserCount++;
            } else {
                // Fetch a random address from API
                RestTemplate restTemplate = new RestTemplate();
                String url = "https://random-data-api.com/api/v2/users?size=1";
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                Map<String, Object> data = response.getBody();
                Map<String, Object> addressData = (Map<String, Object>) data.get("address");

                Address address = new Address();
                address.setCity((String) addressData.get("city"));
                address.setStreetName((String) addressData.get("street_name"));
                address.setStreetAddress((String) addressData.get("street_address"));
                address.setZipCode((String) addressData.get("zip_code"));
                address.setState((String) addressData.get("state"));
                address.setCountry((String) addressData.get("country"));

                Map<String, Object> coordinatesData = (Map<String, Object>) addressData.get("coordinates");
                Coordinates coordinates = new Coordinates();
                coordinates.setLat((Double) coordinatesData.get("lat"));
                coordinates.setLng((Double) coordinatesData.get("lng"));
                address.setCoordinates(coordinates);



                // Add user to the database
                String insertSql = "INSERT INTO myUser (name, gender, mobileNumber, address, active, createdTime) VALUES (?, ?, ?, ?::json, ?,?)";
                UUID id = UUID.randomUUID();
                String createdTime = LocalDateTime.now().toString();
                ObjectMapper objectMapper = new ObjectMapper();
                String addressJson;
                try {
                    addressJson = objectMapper.writeValueAsString(address);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error serializing address object to JSON", e);
                }
                Object[] values = {user.getName(), user.getGender(), user.getMobileNumber(), addressJson,user.isActive(),currentTime};
                jdbcTemplate.update(insertSql, values);

                createdUserCount++;
            }
        }

        if (duplicateUserCount == 0) {
            return createdUserCount + " users created successfully";
        } else {
            return createdUserCount + " users created, " + duplicateUserCount + " users already exist in the database";
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


    public User getUserById(UUID id) {
        String sql = "SELECT * FROM myUser WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @Override
    public String updateUser(List<User> userList) {
        int updatedUserCount = 0;
        int noUpdateCount = 0;
        int notExistingUserCount = 0;

        for (User user : userList) {
            String selectSql = "SELECT * FROM myUser WHERE name = ? AND mobileNumber = ? AND id <> ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, user.getName(), user.getMobileNumber(), user.getId());

            if (!rows.isEmpty()) {
                // User with given name and mobile number already exists in the database, so skip updating this user
                noUpdateCount++;
            } else {
                // Check if user with given ID exists
                String selectedUserList = "SELECT * FROM myUser WHERE id = ?";
                List<Map<String, Object>> selectedUser = jdbcTemplate.queryForList(selectedUserList, user.getId());

                if (selectedUser.isEmpty()) {
                    // User with given ID does not exist in the database
                    notExistingUserCount++;
                } else {
                    // Update user in the database
                    Map<String, Object> row = selectedUser.get(0);
                    UUID id = (UUID) row.get("id");

                    String name = user.getName() != null ? user.getName() : (String) row.get("name");
                    String gender = user.getGender() != null ? user.getGender() : (String) row.get("gender");
                    String mobileNumber = user.getMobileNumber() != null ? user.getMobileNumber() : (String) row.get("mobileNumber");

                    Address address = user.getAddress() != null ? user.getAddress() : null;
                    if (address == null) {
                        // If the address field in the User object is null, use the existing address from the database
                        Object addressObj = row.get("address");
                        if (addressObj != null) {
                            String addressJson = addressObj.toString();
                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                address = objectMapper.readValue(addressJson, Address.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("Error deserializing address object from JSON from existing user", e);
                            }
                        }
                    }

                    boolean isActive = user.isActive() != null ? user.isActive() : (boolean) row.get("active");

                    String updateSql = "UPDATE myUser SET name = ?, gender = ?, mobileNumber = ?, address = ?::json, active = ? WHERE id = ?";
                    ObjectMapper objectMapper = new ObjectMapper();

                    Object[] values;
                    try {
                        values = new Object[]{name, gender, mobileNumber, address != null ? objectMapper.writeValueAsString(address) : null, isActive, id};
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error serializing address object to JSON", e);
                    }

                    jdbcTemplate.update(updateSql, values);

                    updatedUserCount++;
                }
            }
        }

        return String.format("Updated %d users, skipped %d users, and %d users did not exist", updatedUserCount, noUpdateCount, notExistingUserCount);
    }

    @Override
    public String deleteUser(UUID id) {
        String sql = "DELETE FROM myUser WHERE id=?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            return "No user exists with id " + id;
        }
        return "User of id " + id + " has been deleted.";
    }
}