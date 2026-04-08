package com.jobportal.repository;

import com.jobportal.model.Question;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

@Repository
public class QuestionRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Question> rowMapper = (rs, rowNum) -> {
        Question q = new Question();
        q.setId(rs.getLong("id"));
        long tsId = rs.getLong("test_session_id");
        q.setTestSessionId(rs.wasNull() ? null : tsId);
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        return q;
    };

    public Question save(Question q) {
        String sql = "INSERT INTO questions (test_session_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            if (q.getTestSessionId() != null) {
                ps.setLong(1, q.getTestSessionId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectAnswer());
            return ps;
        }, keyHolder);

        // Safe: H2 may return multiple generated keys, so we get ID specifically
        q.setId(((Number) keyHolder.getKeys().get("ID")).longValue());
        return q;
    }

    public List<Question> findByTestSessionId(Long testSessionId) {
        return jdbcTemplate.query("SELECT * FROM questions WHERE test_session_id = ?", rowMapper, testSessionId);
    }

    // Global question bank questions (test_session_id IS NULL)
    public List<Question> findGlobalQuestions() {
        return jdbcTemplate.query("SELECT * FROM questions WHERE test_session_id IS NULL", rowMapper);
    }

    // Fetch N random questions from global bank
    public List<Question> findRandomQuestions(int count) {
        String sql = "SELECT * FROM questions WHERE test_session_id IS NULL ORDER BY RANDOM() LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, count);
    }

    public List<Question> findByIds(List<Long> ids) {
        String placeholders = String.join(",", ids.stream().map(String::valueOf).toList());
        String sql = "SELECT * FROM questions WHERE id IN (" + placeholders + ")";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Question> findAll() {
        return jdbcTemplate.query("SELECT * FROM questions", rowMapper);
    }

    public void deleteByTestSessionId(Long testSessionId) {
        jdbcTemplate.update("DELETE FROM questions WHERE test_session_id = ?", testSessionId);
    }
}
