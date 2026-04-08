package com.jobportal.repository;

import com.jobportal.model.Application;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Application> simpleRowMapper = (rs, rowNum) -> {
        Application app = new Application();
        app.setId(rs.getLong("id"));
        app.setUserId(rs.getLong("user_id"));
        app.setJobId(rs.getLong("job_id"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        return app;
    };

    private final RowMapper<Application> joinedRowMapper = (rs, rowNum) -> {
        Application app = new Application();
        app.setId(rs.getLong("id"));
        app.setUserId(rs.getLong("user_id"));
        app.setJobId(rs.getLong("job_id"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        app.setUserName(rs.getString("user_name"));
        app.setUserEmail(rs.getString("user_email"));
        app.setJobTitle(rs.getString("job_title"));
        return app;
    };

    public Application save(Application application) {
        String sql = "INSERT INTO applications (user_id, job_id, status) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, application.getUserId());
            ps.setLong(2, application.getJobId());
            ps.setString(3, application.getStatus() != null ? application.getStatus() : "PENDING");
            return ps;
        }, keyHolder);

        // Safe: H2 may return multiple generated keys, so we get ID specifically
        application.setId(((Number) keyHolder.getKeys().get("ID")).longValue());
        return application;
    }

    public List<Application> findByUserId(Long userId) {
        String sql = "SELECT a.*, u.name as user_name, u.email as user_email, j.title as job_title " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "WHERE a.user_id = ? ORDER BY a.applied_at DESC";
        return jdbcTemplate.query(sql, joinedRowMapper, userId);
    }

    public List<Application> findByJobId(Long jobId) {
        String sql = "SELECT a.*, u.name as user_name, u.email as user_email, j.title as job_title " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "WHERE a.job_id = ? ORDER BY a.applied_at DESC";
        return jdbcTemplate.query(sql, joinedRowMapper, jobId);
    }

    public List<Application> findAll() {
        String sql = "SELECT a.*, u.name as user_name, u.email as user_email, j.title as job_title " +
                     "FROM applications a " +
                     "JOIN users u ON a.user_id = u.id " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "ORDER BY a.applied_at DESC";
        return jdbcTemplate.query(sql, joinedRowMapper);
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE applications SET status = ? WHERE id = ?", status, id);
    }

    public boolean hasApplied(Long userId, Long jobId) {
        String sql = "SELECT COUNT(*) FROM applications WHERE user_id = ? AND job_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, jobId);
        return count != null && count > 0;
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Integer.class);
        return count != null ? count : 0;
    }

    public int countByUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications WHERE user_id = ?", Integer.class, userId);
        return count != null ? count : 0;
    }
}
