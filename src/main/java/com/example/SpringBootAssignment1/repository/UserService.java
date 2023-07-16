package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;


import java.util.List;
import java.util.UUID;

public interface UserService {
    boolean createUser(User user);

    List<User> searchUsers(UserSearchCriteria criteria);

    boolean updateUser(User user);

    String deleteUser(UUID id);

    List<User> getAllUsers();

    List<User> getActiveUsers();

    List<User> getInActiveUsers();

    boolean userExists(String name, String mobileNumber);
    boolean isDuplicateUser(User user);

    boolean userExistsById(UUID userId);
}