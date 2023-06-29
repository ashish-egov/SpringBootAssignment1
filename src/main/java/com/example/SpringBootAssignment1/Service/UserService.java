package com.example.SpringBootAssignment1.Service;

import com.example.SpringBootAssignment1.Model.MyUser;

import java.util.List;

public interface UserService {
    MyUser createUser(MyUser myUser);

    List<MyUser> searchUsers(UserSearchCriteria criteria);

    MyUser updateUser(Long id, MyUser myUser);

    String deleteUser(Long id);

    List<MyUser> getAllUsers();

    List<MyUser> getActiveUsers();

    List<MyUser> getInactiveUsers();
}