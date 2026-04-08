package com.jobportal.repository;

import com.jobportal.model.TestSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
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
        session.setTitle(rs.getString("title"));
        long jobId = rs.getLong("job_id");
        session.setJobId(rs.wasNull() ? null : jobId);
        long createdBy = rs.getLong("created_by");
        session.setCreatedBy(rs.wasNull() ? null : createdBy);
        long assignedUserId = rs.getLong("assigned_user_id");
        session.setAssignedUserId(rs.wasNull() ? null : assignedUserId);
        long userId = rs.getLong("user_id");
        session.setUserId(rs.wasNull() ? null : userId);
        session.setQuestionIds(rs.getString("question_ids"));
        session.setStatus(rs.getString("status"));
        session.setStartedAt(rs.getTimestamp("started_at"));
        session.setCompletedAt(rs.getTimestamp("completed_at"));
        return session;
    };

    public TestSession save(TestSession session) {
        String sql = "INSERT INTO test_sessions (title, job_id, created_by, assigned_user_id, user_id, question_ids, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, session.getTitle());
            if (session.getJobId() != null) ps.setLong(2, session.getJobId());
            else ps.setNull(2, Types.BIGINT);
            if (session.getCreatedBy() != null) ps.setLong(3, session.getCreatedBy());
            else ps.setNull(3, Types.BIGINT);
            if (session.getAssignedUserId() != null) ps.setLong(4, session.getAssignedUserId());
            else ps.setNull(4, Types.BIGINT);
            if (session.getUserId() != null) ps.setLong(5, session.getUserId());
            else ps.setNull(5, Types.BIGINT);
            ps.setString(6, session.getQuestionIds());
            ps.setString(7, session.getStatus() != null ? session.getStatus() : "ASSIGNED");
            return ps;
        }, keyHolder);

        // Safe: H2 may return multiple generated keys, so we get ID specifically
        session.setId(((Number) keyHolder.getKeys().get("ID")).longValue());
        return session;
    }

    public Optional<TestSession> findById(Long id) {
        String sql = "SELECT * FROM test_sessions WHERE id = ?";
        List<TestSession> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void markInProgress(Long id) {
        jdbcTemplate.update("UPDATE test_sessions SET status = 'IN_PROGRESS' WHERE id = ?", id);
    }

    public void markCompleted(Long id) {
        String sql = "UPDATE test_sessions SET status = 'COMPLETED', completed_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, new Timestamp(System.currentTimeMillis()), id);
    }

    public List<TestSession> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM test_sessions WHERE user_id = ? ORDER BY started_at DESC", rowMapper, userId);
    }

    public List<TestSession> findByAssignedUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM test_sessions WHERE assigned_user_id = ? ORDER BY started_at DESC", rowMapper, userId);
    }

    public List<TestSession> findByCreatedBy(Long userId) {
        return jdbcTemplate.query("SELECT * FROM test_sessions WHERE created_by = ? ORDER BY started_at DESC", rowMapper, userId);
    }

    // Row mapper with joined user name for admin view
    private final RowMapper<TestSession> joinedRowMapper = (rs, rowNum) -> {
        TestSession session = new TestSession();
        session.setId(rs.getLong("id"));
        session.setTitle(rs.getString("title"));
        long jobId = rs.getLong("job_id");
        session.setJobId(rs.wasNull() ? null : jobId);
        long createdBy = rs.getLong("created_by");
        session.setCreatedBy(rs.wasNull() ? null : createdBy);
        long assignedUserId = rs.getLong("assigned_user_id");
        session.setAssignedUserId(rs.wasNull() ? null : assignedUserId);
        long userId = rs.getLong("user_id");
        session.setUserId(rs.wasNull() ? null : userId);
        session.setQuestionIds(rs.getString("question_ids"));
        session.setStatus(rs.getString("status"));
        session.setStartedAt(rs.getTimestamp("started_at"));
        session.setCompletedAt(rs.getTimestamp("completed_at"));
        // Joined fields
        session.setAssignedUserName(rs.getString("assigned_user_name"));
        return session;
    };

    public List<TestSession> findAll() {
        String sql = "SELECT ts.*, u.name as assigned_user_name " +
                     "FROM test_sessions ts " +
                     "LEFT JOIN users u ON ts.assigned_user_id = u.id " +
                     "ORDER BY ts.id DESC";
        return jdbcTemplate.query(sql, joinedRowMapper);
    }

    public int countByCreatedBy(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_sessions WHERE created_by = ?", Integer.class, userId);
        return count != null ? count : 0;
    }

    public int countByAssignedUserId(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_sessions WHERE assigned_user_id = ?", Integer.class, userId);
        return count != null ? count : 0;
    }

    public int countAll() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test_sessions", Integer.class);
        return count != null ? count : 0;
    }

    // Update the question_ids column after questions are saved
    public void updateQuestionIds(Long id, String questionIds) {
        jdbcTemplate.update("UPDATE test_sessions SET question_ids = ? WHERE id = ?", questionIds, id);
    }
}
