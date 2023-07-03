package com.example.SpringBootAssignment1.repository;

import com.example.SpringBootAssignment1.web.Model.User;
import com.example.SpringBootAssignment1.web.Model.UserSearchCriteria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                "CREATE TABLE IF NOT EXISTS myUser (id SERIAL, name VARCHAR(255), gender VARCHAR(255), mobileNumber VARCHAR(255), address VARCHAR(255), active BOOLEAN, PRIMARY KEY (id, active)) PARTITION BY LIST (active);");
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
        String sql = "INSERT INTO myUser (name, gender, mobileNumber, address, active) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, user.getName());
            ps.setString(2, user.getGender());
            ps.setString(3, user.getMobileNumber());
            ps.setString(4, user.getAddress());
            ps.setBoolean(5, user.isActive());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
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

    @Override
    public User updateUser(Long id, User user) {
        String sql = "UPDATE myUser SET name=?, gender=?, mobileNumber=?, address=?, active=? WHERE id=?";
        jdbcTemplate.update(sql, user.getName(), user.getGender(), user.getMobileNumber(), user.getAddress(),
                user.isActive(), id);
        user.setId(id);
        return user;
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
            return user;
        }
    }

}