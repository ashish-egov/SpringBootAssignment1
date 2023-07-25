package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.repository.functions.*;
import com.example.SpringBootAssignment1.repository.mappers.UserRowMapper;
import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserTableCreator userTableCreator;

    @Autowired
    private UserCreator userCreator;

    @Autowired
    private UserSearcher userSearcher;

    @Autowired
    private UserUpdater userUpdater;

    @Autowired
    private UserDeleter userDeleter;

    @PostConstruct
    public void createTable() {
        userTableCreator.createTables();
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM myUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public List<User> getActiveUsers() {
        String sql = "SELECT * FROM activeUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public List<User> getInActiveUsers() {
        String sql = "SELECT * FROM inActiveUser";
        return jdbcTemplate.query(sql, new UserRowMapper());
    }

    @Override
    public boolean createUser(User user) {
        return userCreator.createUser(user);
    }

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        return userSearcher.searchUsers(criteria);
    }

    @Override
    public boolean updateUser(User user) {
        return userUpdater.updateUser(user);
    }

    @Override
    public String deleteUser(String id) {
        return userDeleter.deleteUser(id);
    }

    @Override
    public boolean userExists(String name, String mobileNumber) {
        String sql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, name, mobileNumber);
        return count > 0;
    }

    @Override
    public boolean isDuplicateUser(User user) {
        String selectSql = "SELECT COUNT(*) FROM myUser WHERE name = ? AND mobileNumber = ? AND id <> ?";
        int count = jdbcTemplate.queryForObject(selectSql, Integer.class, user.getName(), user.getMobileNumber(), user.getId());
        return count > 0;
    }

    @Override
    public boolean userExistsById(String userId) {
        String selectedUserList = "SELECT COUNT(*) FROM myUser WHERE id = ?";
        int count = jdbcTemplate.queryForObject(selectedUserList, Integer.class, userId);
        return count > 0;
    }
}