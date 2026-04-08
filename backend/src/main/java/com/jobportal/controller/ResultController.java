package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.service.ResultService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// =============================================
// RESULT CONTROLLER — Handles viewing and managing test results
// Admin: view all results, update status (SELECTED/REJECTED)
// Student: view own results and scores
// =============================================
@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    // ── STUDENT ENDPOINTS ───────────────────────────

    // GET /api/results/user/{userId} — Student views their own test results
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Result>>> getResultsByUser(@PathVariable Long userId) {
        List<Result> results = resultService.getResultsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Results fetched", results));
    }

    // GET /api/results/session/{sessionId} — Get result for a specific test session
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<Result>> getResultBySession(@PathVariable Long sessionId) {
        try {
            Result result = resultService.getResultBySessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Result found", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ── ADMIN ENDPOINTS ─────────────────────────────

    // GET /api/results/all — Admin views ALL student results
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Result>>> getAllResults() {
        List<Result> results = resultService.getAllResults();
        return ResponseEntity.ok(ApiResponse.success("All results fetched", results));
    }

    // PUT /api/results/{id}/status — Admin updates result status (SELECTED / REJECTED)
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateResultStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            String status = (String) body.get("status");
            Long adminId = Long.valueOf(body.get("adminId").toString());
            resultService.updateResultStatus(id, status, adminId);
            return ResponseEntity.ok(ApiResponse.success("Result status updated to " + status, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
