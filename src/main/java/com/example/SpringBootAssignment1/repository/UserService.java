package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {
    String createUser(List<User> userList);

    List<User> searchUsers(UserSearchCriteria criteria);

    String updateUser(List<User> userList);

    String deleteUser(UUID id);

    List<User> getAllUsers();

    List<User> getActiveUsers();

    List<User> getInActiveUsers();
}