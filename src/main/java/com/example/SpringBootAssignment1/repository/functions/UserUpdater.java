package com.example.SpringBootAssignment1.repository.functions;

import com.example.SpringBootAssignment1.web.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.SpringBootAssignment1.web.Model.Address;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class UserUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean updateUser(User user) {
        String updateSql = "UPDATE myUser SET name = ?, gender = ?, mobileNumber = ?, address = ?::json, active = ? WHERE id = ?";
        String id = user.getId();
        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM myUser WHERE id = ?", id);
        String name = user.getName() != null ? user.getName() : (String) row.get("name");
        String gender = user.getGender() != null ? user.getGender() : (String) row.get("gender");
        String mobileNumber = user.getMobileNumber() != null ? user.getMobileNumber() : (String) row.get("mobileNumber");
        Address address = user.getAddress();
        if (address == null) {
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
        ObjectMapper objectMapper = new ObjectMapper();
        Object[] values;
        try {
            values = new Object[] { name, gender, mobileNumber, address != null ? objectMapper.writeValueAsString(address) : null, isActive, id };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing address object to JSON", e);
        }
        int rowsUpdated = jdbcTemplate.update(updateSql, values);
        return rowsUpdated > 0;
    }
}
