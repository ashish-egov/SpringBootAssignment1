package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
                "CREATE TABLE IF NOT EXISTS myUser (id SERIAL, name VARCHAR(255), gender VARCHAR(255), mobileNumber VARCHAR(255), address VARCHAR(255), active BOOLEAN, createdTime BIGINT, PRIMARY KEY (id, active)) PARTITION BY LIST (active);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS activeUser PARTITION OF myUser FOR VALUES IN (TRUE);");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS inActiveUser PARTITION OF myUser FOR VALUES IN (FALSE);");
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
    public User createUser(User user) {
        String sql = "INSERT INTO myUser (name, gender, mobileNumber, address, active, createdTime) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Long currentTime = Instant.now().getEpochSecond();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, user.getName());
            ps.setString(2, user.getGender());
            ps.setString(3, user.getMobileNumber());
            ps.setString(4, user.getAddress());
            ps.setBoolean(5, user.isActive());
            ps.setLong(6, currentTime);
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        user.setCreatedTime(currentTime);
        return user;
    }

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        String sql = "SELECT * FROM myUser";
        List<Object> args = new ArrayList<>();
        boolean hasCriteria = false;

        if (criteria.getId() != null) {
            sql += " WHERE id = ?";
            args.add(criteria.getId());
            hasCriteria = true;
        }

        if (criteria.getMobileNumber() != null) {
            if (hasCriteria) {
                sql += " AND mobileNumber = ?";
            } else {
                sql += " WHERE mobileNumber = ?";
                hasCriteria = true;
            }
            args.add(criteria.getMobileNumber());
        }

        if (criteria.getActive() != null) {
            if (hasCriteria) {
                sql += " AND active = ?";
            } else {
                sql += " WHERE active = ?";
                hasCriteria = true;
            }
            args.add(criteria.getActive());
        }

        return jdbcTemplate.query(sql, args.toArray(), new UserRowMapper());
    }


    public User getUserById(Long id) {
        String sql = "SELECT * FROM myUser WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        if (existingUser == null) {
            return null;
        }
        String name = user.getName() != null ? user.getName() : existingUser.getName();
        String gender = user.getGender() != null ? user.getGender() : existingUser.getGender();
        String mobileNumber = user.getMobileNumber() != null ? user.getMobileNumber() : existingUser.getMobileNumber();
        String address = user.getAddress() != null ? user.getAddress() : existingUser.getAddress();
        boolean active = user.isActive() != null ? user.isActive() : existingUser.isActive();
        Long createdTime = existingUser.getCreatedTime();
        existingUser.setId(id);
        existingUser.setName(name);
        existingUser.setGender(gender);
        existingUser.setMobileNumber(mobileNumber);
        existingUser.setAddress(address);
        existingUser.setActive(active);
        existingUser.setCreatedTime(createdTime);
        String sql = "UPDATE myUser SET name=?, gender=?, mobileNumber=?, address=?, active=?, createdTime=? WHERE id=?";
        jdbcTemplate.update(sql, name, gender, mobileNumber, address, active, createdTime, id);
        return existingUser;
    }

    @Override
    public String deleteUser(Long id) {
        String sql = "DELETE FROM myUser WHERE id=?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            return "No user exists with id " + id;
        }
        return "User of id " + id + " has been deleted.";
    }

    public class UserRowMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setGender(rs.getString("gender"));
            user.setMobileNumber(rs.getString("mobileNumber"));
            user.setAddress(rs.getString("address"));
            user.setActive(rs.getBoolean("active"));
            user.setCreatedTime(rs.getLong("createdTime"));
            return user;
        }
    }

}