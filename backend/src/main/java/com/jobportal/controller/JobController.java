package com.jobportal.controller;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// =============================================
// JOB CONTROLLER — Handles job CRUD and applications
// Admin: create/update/delete jobs, view all applications, update status
// Student: view jobs, apply for jobs, view own applications
// =============================================
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // ── JOB CRUD ────────────────────────────────────

    // POST /api/jobs — Admin creates a new job posting
    // Request body must include "createdBy" (admin user ID)
    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {
        try {
            // Pass createdBy as the admin ID for role validation
            Job saved = jobService.createJob(job, job.getCreatedBy());
            return ResponseEntity.ok(ApiResponse.success("Job created successfully", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/jobs — List all available jobs (both roles can view)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched", jobs));
    }

    // GET /api/jobs/{id} — Get a specific job by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJob(@PathVariable Long id) {
        try {
            Job job = jobService.getJobById(id);
            return ResponseEntity.ok(ApiResponse.success("Job found", job));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // PUT /api/jobs/{id} — Admin updates a job posting
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> updateJob(@PathVariable Long id, @RequestBody Job job) {
        try {
            Job updated = jobService.updateJob(id, job, job.getCreatedBy());
            return ResponseEntity.ok(ApiResponse.success("Job updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /api/jobs/{id} — Admin deletes a job posting
    // Expects "adminId" as a query parameter
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJob(@PathVariable Long id, @RequestParam Long adminId) {
        try {
            jobService.deleteJob(id, adminId);
            return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── APPLICATION ENDPOINTS ────────────────────────

    // POST /api/jobs/apply — Student applies for a job
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

    // GET /api/jobs/applications/{userId} — Get applications for a specific user
    @GetMapping("/applications/{userId}")
    public ResponseEntity<ApiResponse<List<Application>>> getUserApplications(@PathVariable Long userId) {
        List<Application> apps = jobService.getUserApplications(userId);
        return ResponseEntity.ok(ApiResponse.success("Applications fetched", apps));
    }

    // GET /api/jobs/all-applications — Admin views ALL applications across all jobs
    @GetMapping("/all-applications")
    public ResponseEntity<ApiResponse<List<Application>>> getAllApplications() {
        List<Application> apps = jobService.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success("All applications fetched", apps));
    }

    // PUT /api/jobs/application-status — Admin updates application status (SELECTED/REJECTED)
    @PutMapping("/application-status")
    public ResponseEntity<ApiResponse<String>> updateApplicationStatus(@RequestBody Map<String, Object> body) {
        try {
            Long applicationId = Long.valueOf(body.get("applicationId").toString());
            String status = (String) body.get("status");
            Long adminId = Long.valueOf(body.get("adminId").toString());
            jobService.updateApplicationStatus(applicationId, status, adminId);
            return ResponseEntity.ok(ApiResponse.success("Application status updated", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
