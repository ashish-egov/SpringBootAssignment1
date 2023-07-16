package com.example.SpringBootAssignment1.consumer;

import com.example.SpringBootAssignment1.repository.UserService;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserConsumer {

    private final UserService userService;

    public UserConsumer(UserService userService){
        this.userService=userService;
    }


    @KafkaListener(topics = "user-topic-create")
    public void consumeUsersCreate(String userJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(userJson, User.class);

            boolean result=userService.createUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "user-topic-update")
    public void consumeUsersUpdate(String userJson) {
        try {
            // Deserialize the JSON string to a User object
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(userJson, User.class);

            // Update the user in the database
            boolean result=userService.updateUser(user);
        } catch (Exception e) {
            // Log the exception and handle it appropriately
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "user-topic-delete")
    public void consumeUsersDelete(String userIdJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UUID userId = mapper.readValue(userIdJson, UUID.class);
            userService.deleteUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}