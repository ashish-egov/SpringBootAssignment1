package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.client.RestTemplate;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserCreator {
    private final JdbcTemplate jdbcTemplate;

    public UserCreator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> createUsers(List<User> userList) throws UserCreationException {
        List<User> createdUserList = new ArrayList<>();
        List<User> duplicateUserList = new ArrayList<>();

        String sql = "INSERT INTO myUser (name, gender, mobileNumber, address, active, createdTime) VALUES (?, ?, ?, ?::json, ?, ?)";
        Long currentTime = Instant.now().getEpochSecond();

        for (User user : userList) {
            try {
                User createdUser = createUser(user, sql, currentTime);
                createdUserList.add(createdUser);
            } catch (DataIntegrityViolationException e) {
                duplicateUserList.add(user);
            }
        }

        if (duplicateUserList.isEmpty()) {
            return createdUserList;
        } else {
            throw new UserCreationException(createdUserList, duplicateUserList);
        }
    }

    private User createUser(User user, String sql, Long currentTime) {
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

        user.setAddress(address);
        String addressJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            addressJson = objectMapper.writeValueAsString(user.getAddress());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing address object to JSON", e);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getGender());
            ps.setString(3, user.getMobileNumber());
            ps.setString(4, addressJson);
            ps.setBoolean(5, user.isActive());
            ps.setLong(6, currentTime);
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        user.setCreatedTime(currentTime);

        return user;
    }
}