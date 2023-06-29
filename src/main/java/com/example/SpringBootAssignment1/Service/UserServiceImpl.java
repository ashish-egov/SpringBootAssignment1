package com.example.SpringBootAssignment1.Service;

import com.example.SpringBootAssignment1.Model.MyUser;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createTable() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS my_user (id SERIAL, name VARCHAR(255), gender VARCHAR(255), mobile_number VARCHAR(255), address VARCHAR(255), active BOOLEAN, PRIMARY KEY (id, active)) PARTITION BY LIST (active);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS active_user PARTITION OF my_user FOR VALUES IN (TRUE);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS inactive_user PARTITION OF my_user FOR VALUES IN (FALSE);");
    }

    @Override
    public List<MyUser> getAllUsers() {
        String sql = "SELECT * FROM my_user";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MyUser.class));
    }

    @Override
    public List<MyUser> getActiveUsers() {
        String sql = "SELECT * FROM active_user";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MyUser.class));
    }

    @Override
    public List<MyUser> getInactiveUsers() {
        String sql = "SELECT * FROM inactive_user";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MyUser.class));
    }

    @Override
    public MyUser createUser(MyUser myUser) {
        String sql = "INSERT INTO my_user (name, gender, mobile_number, address, active) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, myUser.getName());
            ps.setString(2, myUser.getGender());
            ps.setString(3, myUser.getMobileNumber());
            ps.setString(4, myUser.getAddress());
            ps.setBoolean(5, myUser.isActive());
            return ps;
        }, keyHolder);

        myUser.setId(keyHolder.getKey().longValue());
        return myUser;
    }

    @Override
    public List<MyUser> searchUsers(UserSearchCriteria criteria) {
        String sql = "SELECT * FROM my_user WHERE 1=1";
        List<Object> args = new ArrayList<>();

        if (criteria.getId() != null) {
            sql += " AND id = ?";
            args.add(criteria.getId());
        }

        if (criteria.getMobileNumber() != null) {
            sql += " AND mobile_number = ?";
            args.add(criteria.getMobileNumber());
        }

        return jdbcTemplate.query(sql, args.toArray(), new BeanPropertyRowMapper<>(MyUser.class));
    }

    @Override
    public MyUser updateUser(Long id, MyUser myUser) {
        String sql = "UPDATE my_user SET name=?, gender=?, mobile_number=?, address=?, active=? WHERE id=?";
        jdbcTemplate.update(sql, myUser.getName(), myUser.getGender(), myUser.getMobileNumber(), myUser.getAddress(),
                myUser.isActive(), id);
        myUser.setId(id);
        return myUser;
    }

    @Override
    public String deleteUser(Long id) {
        String sql = "DELETE FROM my_user WHERE id=?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            return "No user exists with id " + id;
        }
        return "User of id " + id + " has been deleted.";
    }

}