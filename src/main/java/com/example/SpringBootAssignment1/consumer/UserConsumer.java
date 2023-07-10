package com.example.SpringBootAssignment1.consumer;

import com.example.SpringBootAssignment1.repository.UserService;
import com.example.SpringBootAssignment1.web.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.List;

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

            userService.createUser(user);
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
            userService.updateUser(user);
        } catch (Exception e) {
            // Log the exception and handle it appropriately
            e.printStackTrace();
        }
    }
}