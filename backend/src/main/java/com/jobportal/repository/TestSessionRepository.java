package com.jobportal.repository;

import com.jobportal.model.TestSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class TestSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TestSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TestSession> rowMapper = (rs, rowNum) -> {
        TestSession session = new TestSession();
        session.setId(rs.getLong("id"));
        session.setUserId(rs.getLong("user_id"));
        session.setQuestionIds(rs.getString("question_ids"));
        session.setStatus(rs.getString("status"));
        session.setStartedAt(rs.getTimestamp("started_at"));
        session.setCompletedAt(rs.getTimestamp("completed_at"));
        return session;
    };

    public TestSession save(TestSession session) {
        String sql = "INSERT INTO test_sessions (user_id, question_ids, status) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, session.getUserId());
            ps.setString(2, session.getQuestionIds());
            ps.setString(3, "IN_PROGRESS");
            return ps;
        }, keyHolder);

        session.setId(keyHolder.getKey().longValue());
        return session;
    }

    public Optional<TestSession> findById(Long id) {
        String sql = "SELECT * FROM test_sessions WHERE id = ?";
        List<TestSession> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void markCompleted(Long id) {
        String sql = "UPDATE test_sessions SET status = 'COMPLETED', completed_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, new Timestamp(System.currentTimeMillis()), id);
    }

    public List<TestSession> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM test_sessions WHERE user_id = ? ORDER BY started_at DESC", rowMapper, userId);
    }
}
