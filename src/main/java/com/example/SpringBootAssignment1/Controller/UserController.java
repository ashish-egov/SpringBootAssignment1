package com.example.SpringBootAssignment1.Controller;

import com.example.SpringBootAssignment1.Model.User;
import com.example.SpringBootAssignment1.Service.UserSearchCriteria;
import com.example.SpringBootAssignment1.Service.UserService;
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
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("_search")
    public List<User> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUsers(criteria);
    }

    @PutMapping("_update/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("_delete/{id}")
    public String deleteUser(@PathVariable Long id, @RequestBody User user) {
        return userService.deleteUser(id);
    }
}