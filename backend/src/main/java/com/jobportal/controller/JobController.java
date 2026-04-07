package com.jobportal.controller;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // POST /api/jobs — Admin creates a job
    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {
        try {
            Job saved = jobService.createJob(job);
            return ResponseEntity.ok(ApiResponse.success("Job created successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/jobs — List all jobs
    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", jobs));
    }

    // GET /api/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJob(@PathVariable Long id) {
        try {
            Job job = jobService.getJobById(id);
            return ResponseEntity.ok(ApiResponse.success("Job found", job));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/jobs/apply — Student applies
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Application>> apply(@RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            Long jobId = body.get("jobId");
            Application app = jobService.applyForJob(userId, jobId);
            return ResponseEntity.ok(ApiResponse.success("Applied successfully", app));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/jobs/applications/{userId}
    @GetMapping("/applications/{userId}")
    public ResponseEntity<ApiResponse<List<Application>>> getUserApplications(@PathVariable Long userId) {
        List<Application> apps = jobService.getUserApplications(userId);
        return ResponseEntity.ok(ApiResponse.success("Applications fetched", apps));
    }
}
