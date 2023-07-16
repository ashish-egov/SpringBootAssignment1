package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
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

    private final UserSearcher userSearcher;

    private UserTableCreator userTableCreator;

    public UserServiceImpl(JdbcTemplate jdbcTemplate,UserTableCreator userTableCreator,UserSearcher userSearcher) {
        this.jdbcTemplate = jdbcTemplate;
        this.userTableCreator=userTableCreator;
        this.userSearcher = userSearcher;
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

    @Value("${api.random-data-url}")
    private String randomDataUrl;
    @Override
    public boolean createUser(User user) {
        Address address = getRandomAddress();
        String currentTime = getFormattedLocalTime();

        String insertSql = "INSERT INTO myUser (name, gender, mobileNumber, address, active, createdTime) VALUES (?, ?, ?, ?::json, ?, ?)";
        ObjectMapper objectMapper = new ObjectMapper();
        String addressJson;
        try {
            addressJson = objectMapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing address object to JSON", e);
        }

        int rowsInserted = jdbcTemplate.update(insertSql, user.getName(), user.getGender(), user.getMobileNumber(), addressJson, user.isActive(), currentTime);
        return rowsInserted > 0;
    }

    private Address getRandomAddress() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.getForEntity(randomDataUrl, Map.class);
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

        return address;
    }

    private String getFormattedLocalTime() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
          return userSearcher.searchUsers(criteria);
    }


    public User getUserById(UUID id) {
        String sql = "SELECT * FROM myUser WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public boolean updateUser(User user) {
        // Update user in the database
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM myUser WHERE id = ?", user.getId());

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

        return true;
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

    @Override
    public boolean userExists(String name, String mobileNumber) {
        String sql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, name, mobileNumber);
        return count > 0;
    }

    @Override
    public boolean isDuplicateUser(User user) {
        String selectSql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ? AND id <> ?";
        int count = jdbcTemplate.queryForObject(selectSql, Integer.class, user.getName(), user.getMobileNumber(), user.getId());
        return count > 0;
    }

    @Override
    public boolean userExistsById(UUID userId) {
        String selectedUserList = "SELECT COUNT(*) FROM myUser WHERE id = ?";
        int count = jdbcTemplate.queryForObject(selectedUserList, Integer.class, userId);
        return count > 0;
    }
}