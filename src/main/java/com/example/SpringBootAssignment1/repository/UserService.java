package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;

import java.util.List;

public interface UserService {
    User createUser(User user);

    List<User> searchUsers(UserSearchCriteria criteria);

    User updateUser(Long id, User user);

    String deleteUser(Long id);

    List<User> getAllUsers();

    List<User> getActiveUsers();

    List<User> getInActiveUsers();
}