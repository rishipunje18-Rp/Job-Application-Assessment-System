package com.jobportal.service;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // ── Job CRUD (Admin only) ──

    public Job createJob(Job job, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can create jobs");
        }
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Job title is required");
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Job description is required");
        }
        if (job.getCompany() == null || job.getCompany().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }
        job.setCreatedBy(adminId);
        return jobRepository.save(job);
    }

    public Job updateJob(Long jobId, Job updated, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can update jobs");
        }
        Job existing = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        existing.setTitle(updated.getTitle() != null ? updated.getTitle() : existing.getTitle());
        existing.setDescription(updated.getDescription() != null ? updated.getDescription() : existing.getDescription());
        existing.setCompany(updated.getCompany() != null ? updated.getCompany() : existing.getCompany());
        existing.setLocation(updated.getLocation() != null ? updated.getLocation() : existing.getLocation());
        existing.setSalary(updated.getSalary() != null ? updated.getSalary() : existing.getSalary());

        jobRepository.update(existing);
        return existing;
    }

    public void deleteJob(Long jobId, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can delete jobs");
        }
        jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        jobRepository.deleteById(jobId);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    // ── Applications ──

    public Application applyForJob(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"STUDENT".equals(user.getRole())) {
            throw new RuntimeException("Only students can apply for jobs");
        }

        if (applicationRepository.hasApplied(userId, jobId)) {
            throw new RuntimeException("You have already applied for this job");
        }

        jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(jobId);
        application.setStatus("PENDING");

        return applicationRepository.save(application);
    }

    public List<Application> getUserApplications(Long userId) {
        return applicationRepository.findByUserId(userId);
    }

    public List<Application> getApplicationsByJobId(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public void updateApplicationStatus(Long applicationId, String status, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can update application status");
        }
        if (!status.equals("PENDING") && !status.equals("SELECTED") && !status.equals("REJECTED")) {
            throw new RuntimeException("Status must be PENDING, SELECTED, or REJECTED");
        }
        applicationRepository.updateStatus(applicationId, status);
    }

    // ── Stats ──

    public int countJobs() {
        return jobRepository.countAll();
    }

    public int countApplications() {
        return applicationRepository.countAll();
    }

    public int countUserApplications(Long userId) {
        return applicationRepository.countByUserId(userId);
    }
}
