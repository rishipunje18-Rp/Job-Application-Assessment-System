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
        r.setCompletedAt(rs.getTimestamp("completed_at"));
        return r;
    };

    public Result save(Result result) {
        String sql = "INSERT INTO results (user_id, test_session_id, score, total_questions, percentage) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, result.getUserId());
            ps.setLong(2, result.getTestSessionId());
            ps.setInt(3, result.getScore());
            ps.setInt(4, result.getTotalQuestions());
            ps.setDouble(5, result.getPercentage());
            return ps;
        }, keyHolder);

        result.setId(keyHolder.getKey().longValue());
        return result;
    }

    public List<Result> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM results WHERE user_id = ? ORDER BY completed_at DESC", rowMapper, userId);
    }

    public Optional<Result> findByTestSessionId(Long sessionId) {
        String sql = "SELECT * FROM results WHERE test_session_id = ?";
        List<Result> list = jdbcTemplate.query(sql, rowMapper, sessionId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
