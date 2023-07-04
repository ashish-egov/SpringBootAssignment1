package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.Coordinates;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setName(rs.getString("name"));
        user.setGender(rs.getString("gender"));
        user.setMobileNumber(rs.getString("mobileNumber"));
        String addressJson = rs.getString("address");
        if (addressJson != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Address address = objectMapper.readValue(addressJson, Address.class);
                if (address.getCoordinates() == null) {
                    Coordinates coordinates = new Coordinates();
                    coordinates.setLat(0);
                    coordinates.setLng(0);
                    address.setCoordinates(coordinates);
                }
                user.setAddress(address);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error parsing address JSON", e);
            }
        } else {
            // If address is null, create a new Address object with all fields set to null
            Address address = new Address();
            address.setCity(null);
            address.setState(null);
            address.setZipCode(null);
            Coordinates coordinates = new Coordinates();
            coordinates.setLat(0);
            coordinates.setLng(0);
            address.setCoordinates(coordinates);
            user.setAddress(address);
        }
        user.setActive(rs.getBoolean("active"));
        user.setCreatedTime(rs.getString("createdTime"));
        return user;
    }
}
