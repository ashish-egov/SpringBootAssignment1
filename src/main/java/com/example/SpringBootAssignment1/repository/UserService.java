package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<?> createUser(List<User> userList);

    List<User> searchUsers(UserSearchCriteria criteria);

    List<User> updateUser(List<User> userList);

    String deleteUser(Long id);

    List<User> getAllUsers();

    List<User> getActiveUsers();

    List<User> getInActiveUsers();
}