package com.jobportal.repository;

import com.jobportal.model.Question;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        return q;
    };

    // Fetch 10 random questions for a test
    public List<Question> findRandomQuestions(int count) {
        String sql = "SELECT * FROM questions ORDER BY RANDOM() LIMIT ?";
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
}
