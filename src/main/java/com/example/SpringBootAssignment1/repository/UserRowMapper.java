package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setGender(rs.getString("gender"));
        user.setMobileNumber(rs.getString("mobileNumber"));
        String addressJson = rs.getString("address");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Address address = objectMapper.readValue(addressJson, Address.class);
            user.setAddress(address);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing address JSON", e);
        }
        user.setActive(rs.getBoolean("active"));
        user.setCreatedTime(rs.getLong("createdTime"));
        return user;
    }
}
