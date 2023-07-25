package com.example.SpringBootAssignment1.web.Controller;

import com.example.SpringBootAssignment1.web.Model.DeleteWrapper;
import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.example.SpringBootAssignment1.repository.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.user-create}")
    private String topicCreate;

    @Value("${kafka.topic.user-update}")
    private String topicUpdate;

    @Value("${kafka.topic.user-delete}")
    private String topicDelete;

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
    public ResponseEntity<String> createUser(@RequestBody List<User> userList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<User> usersSentToKafka = new ArrayList<>();
        List<User> usersNotSentToKafka = new ArrayList<>();

        for (User user : userList) {
            if (!userService.userExists(user.getName(), user.getMobileNumber())) {
                user.setId(generateUserId());
                if (user.getId() == null) {
                    return ResponseEntity.badRequest().body("Id-gen not working.");
                }
                String json = mapper.writeValueAsString(user);
                kafkaTemplate.send(topicCreate, json);
                usersSentToKafka.add(user);
            } else {
                usersNotSentToKafka.add(user);
            }
        }

        String responseMessage = "Users request sent to Kafka are:\n" + formatUserList(usersSentToKafka)
                + "\n\nUsers not sent to Kafka are:\n" + formatUserList(usersNotSentToKafka);
        return ResponseEntity.ok(responseMessage);
    }

    @PostMapping("_search")
    public List<User> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUsers(criteria);
    }

    @PatchMapping("_update")
    public ResponseEntity<String> updateUser(@RequestBody List<User> userList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<User> usersSentToKafka = new ArrayList<>();
        List<User> usersNotSentToKafka = new ArrayList<>();

        for (User user : userList) {
            if (!userService.isDuplicateUser(user) && userService.userExistsById(user.getId())) {
                String json = new ObjectMapper().writeValueAsString(user);
                kafkaTemplate.send(topicUpdate, json);
                usersSentToKafka.add(user);
            } else {
                usersNotSentToKafka.add(user);
            }
        }

        String responseMessage = "Users request sent to Kafka are:\n" + formatUserList(usersSentToKafka)
                + "\n\nUsers not sent to Kafka are:\n" + formatUserList(usersNotSentToKafka);
        return ResponseEntity.ok(responseMessage);
    }


    @DeleteMapping("_delete")
    public String deleteUser(@RequestBody DeleteWrapper deleteWrapper) throws JsonProcessingException {
        String id = deleteWrapper.getId();
        if (id != null && userService.userExistsById(id)) {
            String json = new ObjectMapper().writeValueAsString(id);
            kafkaTemplate.send(topicDelete, json);
            return "User delete request sent to Kafka";
        } else {
            return "Invalid or missing user ID in the request body";
        }
    }

    @GetMapping("_deleteall")
    public String deleteAllMessages() {
        kafkaTemplate.send(new ProducerRecord<>(topicCreate, null, "null"));
        kafkaTemplate.send(new ProducerRecord<>(topicUpdate, null, "null"));
        kafkaTemplate.send(new ProducerRecord<>(topicDelete, null, "null"));
        return "All messages from Kafka topics user-create, user-update and user-delete have been deleted.";
    }

    private String generateUserId() {
        RestTemplate restTemplate=new RestTemplate();
        String idGenerationUrl = "http://localhost:8088/egov-idgen/id/_generate";
        String requestJson = "{\"RequestInfo\": {}, \"idRequests\": [{\"tenantId\": \"pb\", \"idName\": \"my_user.user.id\"}]}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        // Make a POST request to the ID generation API using exchange
        ResponseEntity<String> responseEntity = restTemplate.exchange(idGenerationUrl, HttpMethod.POST, requestEntity, String.class);
        String responseJson = responseEntity.getBody();

        // Parse the response to get the generated ID
        String generatedId = null;
        try {
            JsonNode responseNode = new ObjectMapper().readTree(responseJson);
            JsonNode idResponses = responseNode.get("idResponses");
            if (idResponses != null && idResponses.isArray() && idResponses.size() > 0) {
                generatedId = idResponses.get(0).get("id").asText();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    private String formatUserList(List<User> users) {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append("Name: ").append(user.getName()).append(", Mobile Number: ").append(user.getMobileNumber()).append("\n");
        }
        return sb.toString();
    }
}