package com.jobportal.service;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public Job createJob(Job job) {
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Job title is required");
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Job description is required");
        }
        if (job.getCompany() == null || job.getCompany().trim().isEmpty()) {
            throw new RuntimeException("Company name is required");
        }
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public Application applyForJob(Long userId, Long jobId) {
        // Check if already applied
        if (applicationRepository.hasApplied(userId, jobId)) {
            throw new RuntimeException("You have already applied for this job");
        }

        // Verify job exists
        jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Application application = new Application();
        application.setUserId(userId);
        application.setJobId(jobId);
        application.setStatus("APPLIED");

        return applicationRepository.save(application);
    }

    public List<Application> getUserApplications(Long userId) {
        return applicationRepository.findByUserId(userId);
    }
}
