package com.jobportal.repository;

import com.jobportal.model.Job;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JobRepository {

    private final JdbcTemplate jdbcTemplate;

    public JobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Job> rowMapper = (rs, rowNum) -> {
        Job job = new Job();
        job.setId(rs.getLong("id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setCompany(rs.getString("company"));
        job.setLocation(rs.getString("location"));
        job.setSalary(rs.getString("salary"));
        job.setCreatedBy(rs.getLong("created_by"));
        job.setCreatedAt(rs.getTimestamp("created_at"));
        return job;
    };

    public Job save(Job job) {
        String sql = "INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getCompany());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getSalary());
            ps.setLong(6, job.getCreatedBy());
            return ps;
        }, keyHolder);

        job.setId(keyHolder.getKey().longValue());
        return job;
    }

    public List<Job> findAll() {
        return jdbcTemplate.query("SELECT * FROM jobs ORDER BY created_at DESC", rowMapper);
    }

    public Optional<Job> findById(Long id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        List<Job> jobs = jdbcTemplate.query(sql, rowMapper, id);
        return jobs.isEmpty() ? Optional.empty() : Optional.of(jobs.get(0));
    }
}
