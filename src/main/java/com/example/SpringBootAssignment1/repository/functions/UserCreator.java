package com.example.SpringBootAssignment1.repository.functions;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Component
public class UserCreator {

    @Value("${api.random-data-url}")
    private String randomDataUrl;
    private final JdbcTemplate jdbcTemplate;

    public UserCreator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean createUser(User user) {
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

        String currentTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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

}
