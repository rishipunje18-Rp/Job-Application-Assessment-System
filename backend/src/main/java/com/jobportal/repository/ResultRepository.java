package com.jobportal.repository;

import com.jobportal.model.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ResultRepository {

    private final JdbcTemplate jdbcTemplate;

    public ResultRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Result> rowMapper = (rs, rowNum) -> {
        Result r = new Result();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setTestSessionId(rs.getLong("test_session_id"));
        r.setScore(rs.getInt("score"));
        r.setTotalQuestions(rs.getInt("total_questions"));
        r.setPercentage(rs.getDouble("percentage"));
        r.setStatus(rs.getString("status"));
        r.setCompletedAt(rs.getTimestamp("completed_at"));
        return r;
    };

    private final RowMapper<Result> joinedRowMapper = (rs, rowNum) -> {
        Result r = new Result();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setTestSessionId(rs.getLong("test_session_id"));
        r.setScore(rs.getInt("score"));
        r.setTotalQuestions(rs.getInt("total_questions"));
        r.setPercentage(rs.getDouble("percentage"));
        r.setStatus(rs.getString("status"));
        r.setCompletedAt(rs.getTimestamp("completed_at"));
        r.setUserName(rs.getString("user_name"));
        r.setUserEmail(rs.getString("user_email"));
        r.setTestTitle(rs.getString("test_title"));
        return r;
    };

    public Result save(Result result) {
        String sql = "INSERT INTO results (user_id, test_session_id, score, total_questions, percentage, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, result.getUserId());
            ps.setLong(2, result.getTestSessionId());
            ps.setInt(3, result.getScore());
            ps.setInt(4, result.getTotalQuestions());
            ps.setDouble(5, result.getPercentage());
            ps.setString(6, result.getStatus() != null ? result.getStatus() : "PENDING");
            return ps;
        }, keyHolder);

        // Safe: H2 may return multiple generated keys, so we get ID specifically
        result.setId(((Number) keyHolder.getKeys().get("ID")).longValue());
        return result;
    }

    public List<Result> findByUserId(Long userId) {
        String sql = "SELECT r.*, u.name as user_name, u.email as user_email, ts.title as test_title " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN test_sessions ts ON r.test_session_id = ts.id " +
                     "WHERE r.user_id = ? ORDER BY r.completed_at DESC";
        return jdbcTemplate.query(sql, joinedRowMapper, userId);
    }

    public List<Result> findAll() {
        String sql = "SELECT r.*, u.name as user_name, u.email as user_email, ts.title as test_title " +
                     "FROM results r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "LEFT JOIN test_sessions ts ON r.test_session_id = ts.id " +
                     "ORDER BY r.completed_at DESC";
        return jdbcTemplate.query(sql, joinedRowMapper);
    }

    public Optional<Result> findByTestSessionId(Long sessionId) {
        String sql = "SELECT * FROM results WHERE test_session_id = ?";
        List<Result> list = jdbcTemplate.query(sql, rowMapper, sessionId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update("UPDATE results SET status = ? WHERE id = ?", status, id);
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM results", Integer.class);
        return count != null ? count : 0;
    }

    public int countByUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM results WHERE user_id = ?", Integer.class, userId);
        return count != null ? count : 0;
    }
}
