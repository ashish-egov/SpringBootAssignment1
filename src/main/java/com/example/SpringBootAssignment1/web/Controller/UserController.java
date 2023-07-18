package com.example.SpringBootAssignment1.web.Controller;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.example.SpringBootAssignment1.repository.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.user-create}")
    private String topicCreate;

    @Value("${kafka.topic.user-update}")
    private String topicUpdate;

    @Value("${kafka.topic.user-delete}")
    private String topicDelete;

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
                kafkaTemplate.send(topicCreate, json);
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
                kafkaTemplate.send(topicUpdate, json);
            }
        }
        return "User update request sent to Kafka";
    }


    @DeleteMapping("_delete/{id}")
    public String deleteUser(@PathVariable UUID id) throws JsonProcessingException {
        if(userService.userExistsById(id)) {
            String json = new ObjectMapper().writeValueAsString(id);
            kafkaTemplate.send(topicDelete, json);
            return "User delete request sent to Kafka";
        } else {
            return "No user exists with id " + id;
        }
    }

    @GetMapping("_deleteall")
    public String deleteAllMessages() {
        kafkaTemplate.send(new ProducerRecord<>(topicCreate, null, "null"));
        kafkaTemplate.send(new ProducerRecord<>(topicUpdate, null, "null"));
        kafkaTemplate.send(new ProducerRecord<>(topicDelete, null, "null"));
        return "All messages from Kafka topics user-create, user-update and user-delete have been deleted.";
    }
}