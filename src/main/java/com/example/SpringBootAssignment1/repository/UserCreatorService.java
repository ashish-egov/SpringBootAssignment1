package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Component
public class UserCreatorService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean createUser(User user, String currentTime) {
        // Check if user already exists
        String sql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, user.getName(), user.getMobileNumber());

        if (count > 0) {
            return false;
        }

        // Fetch a random address from API
        String url = "https://random-data-api.com/api/v2/users?size=1";
        RestTemplate restTemplate = new RestTemplate();
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
        String insertSql = "INSERT INTO myUser (name, gender, mobileNumber, address, active, createdTime) VALUES (?, ?, ?, ?::json, ?, ?)";
        String addressJson;
        try {
            addressJson = objectMapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing address object to JSON", e);
        }
        Object[] values = {user.getName(), user.getGender(), user.getMobileNumber(), addressJson, user.isActive(), currentTime};
        int rowsAffected = jdbcTemplate.update(insertSql, values);

        return rowsAffected == 1;
    }
}
