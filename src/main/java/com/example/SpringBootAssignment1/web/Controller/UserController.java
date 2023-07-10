package com.example.SpringBootAssignment1.web.Controller;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.example.SpringBootAssignment1.repository.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserController(UserService userService,KafkaTemplate<String, String> kafkaTemplate) {

        this.userService = userService;
        this.kafkaTemplate=kafkaTemplate;
    }

    @GetMapping("_getall")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("_getactive")
    public List<User> getActiveUsers() {
        return userService.getActiveUsers();
    }

    @GetMapping("_getinactive")
    public List<User> getInActiveUsers() {
        return userService.getInActiveUsers();
    }


    @PostMapping("_create")
    public String createUser(@RequestBody List<User> userList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        for (User user : userList) {
            if (!userService.userExists(user.getName(), user.getMobileNumber())) {
                String json = mapper.writeValueAsString(user);
                kafkaTemplate.send("user-topic-create", json);
            }
        }
        return "User creation request sent to Kafka";
    }

    @PostMapping("_search")
    public List<User> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUsers(criteria);
    }

    @PatchMapping("_update")
    public String updateUser(@RequestBody List<User> userList) throws JsonProcessingException {
        for (User user : userList) {
            if (!userService.isDuplicateUser(user) && userService.userExistsById(user.getId())) {
                String json = new ObjectMapper().writeValueAsString(user);
                kafkaTemplate.send("user-topic-update", json);
            }
        }
        return "User update request sent to Kafka";
    }


    @DeleteMapping("_delete/{id}")
    public String deleteUser(@PathVariable UUID id) {
        return userService.deleteUser(id);
    }

    @GetMapping("_deleteall")
    public String deleteAllMessages() {
        kafkaTemplate.send("user-topic-create", "--delete");
        kafkaTemplate.send("user-topic-update", "--delete");
        return "All messages from Kafka topics user-topic-create and user-topic-update have been deleted.";
    }
}