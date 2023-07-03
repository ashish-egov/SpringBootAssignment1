package com.example.SpringBootAssignment1.web.Controller;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import com.example.SpringBootAssignment1.repository.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<?> createUser(@RequestBody List<User> userList) {
        return userService.createUser(userList);
    }

    @PostMapping("_search")
    public List<User> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUsers(criteria);
    }

    @PatchMapping("_update")
    public List<User> updateUser(@RequestBody List<User> userList) {
        return userService.updateUser(userList);
    }

    @DeleteMapping("_delete/{id}")
    public String deleteUser(@PathVariable Long id, @RequestBody User user) {
        return userService.deleteUser(id);
    }
}