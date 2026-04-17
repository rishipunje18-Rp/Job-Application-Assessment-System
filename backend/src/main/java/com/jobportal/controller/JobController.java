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

    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job, @RequestParam Long adminId) {
        try {
            Job saved = jobService.createJob(job, adminId);
            return ResponseEntity.ok(ApiResponse.success("Job created successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> updateJob(@PathVariable Long id, @RequestBody Job job, @RequestParam Long adminId) {
        try {
            Job updated = jobService.updateJob(id, job, adminId);
            return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long id, @RequestParam Long adminId) {
        try {
            jobService.deleteJob(id, adminId);
            return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJob(@PathVariable Long id) {
        try {
            Job job = jobService.getJobById(id);
            return ResponseEntity.ok(ApiResponse.success("Job found", job));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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

    @GetMapping("/applications/{userId}")
    public ResponseEntity<ApiResponse<List<Application>>> getUserApplications(@PathVariable Long userId) {
        List<Application> apps = jobService.getUserApplications(userId);
        return ResponseEntity.ok(ApiResponse.success("Applications fetched", apps));
    }

    @GetMapping("/applications/all")
    public ResponseEntity<ApiResponse<List<Application>>> getAllApplications() {
        List<Application> apps = jobService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success("Applications fetched", apps));
    }

    @GetMapping("/applications/job/{jobId}")
    public ResponseEntity<ApiResponse<List<Application>>> getApplicationsByJobId(@PathVariable Long jobId) {
        List<Application> apps = jobService.getApplicationsByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success("Applications fetched", apps));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestParam Long adminId) {
        try {
            String status = body.get("status");
            jobService.updateApplicationStatus(id, status, adminId);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
