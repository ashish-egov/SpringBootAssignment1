package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;

import java.util.List;

public class UserCreationException extends Exception {
    private final List<User> createdUsers;
    private final List<User> duplicateUsers;

    public UserCreationException(List<User> createdUsers, List<User> duplicateUsers) {
        this.createdUsers = createdUsers;
        this.duplicateUsers = duplicateUsers;
    }

    public List<User> getCreatedUsers() {
        return createdUsers;
    }

    public List<User> getDuplicateUsers() {
        return duplicateUsers;
    }
}
