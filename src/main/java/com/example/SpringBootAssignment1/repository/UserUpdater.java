package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.Address;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
public class UserUpdater {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String updateTheseUsers(List<User> userList){
        int updatedUserCount = 0;
        int noUpdateCount = 0;
        int notExistingUserCount = 0;

        for (User user : userList) {
            String selectedUserList = "SELECT * FROM myUser WHERE id = ?";
            List<Map<String, Object>> selectedUser = jdbcTemplate.queryForList(selectedUserList, user.getId());

            if (selectedUser.isEmpty()) {
                // User with given ID does not exist in the database
                notExistingUserCount++;
            } else {

                String selectSql = "SELECT * FROM myUser WHERE name = ? AND mobileNumber = ? AND id <> ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectSql, user.getName(), user.getMobileNumber(), user.getId());
                if (!rows.isEmpty()) {
                    // User with given name and mobile number already exists in the database, so skip updating this user
                    noUpdateCount++;
                } else {
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
}
