package com.example.SpringBootAssignment1.Controller;

import com.example.SpringBootAssignment1.Model.MyUser;
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
    public List<MyUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("_getactive")
    public List<MyUser> getActiveUsers() {
        return userService.getActiveUsers();
    }

    @GetMapping("_getinactive")
    public List<MyUser> getInactiveUsers() {
        return userService.getInactiveUsers();
    }

    @PostMapping("_create")
    public MyUser createUser(@RequestBody MyUser myUser) {
        return userService.createUser(myUser);
    }

    @PostMapping("_search")
    public List<MyUser> searchUsers(@RequestBody UserSearchCriteria criteria) {
        return userService.searchUsers(criteria);
    }

    @PutMapping("_update/{id}")
    public MyUser updateUser(@PathVariable Long id, @RequestBody MyUser myUser) {
        return userService.updateUser(id, myUser);
    }

    @DeleteMapping("_delete/{id}")
    public String deleteUser(@PathVariable Long id, @RequestBody MyUser myUser) {
        return userService.deleteUser(id);
    }
}