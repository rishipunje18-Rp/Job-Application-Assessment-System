package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.service.ResultService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Result>>> getResultsByUser(@PathVariable Long userId) {
        List<Result> results = resultService.getResultsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Results fetched", results));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<Result>> getResultBySession(@PathVariable Long sessionId) {
        try {
            Result result = resultService.getResultBySessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Result found", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Result>>> getAllResults() {
        List<Result> results = resultService.getAllResults();
        return ResponseEntity.ok(ApiResponse.success("All results fetched", results));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateResultStatus(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestParam Long adminId) {
        try {
            String status = body.get("status");
            resultService.updateResultStatus(id, status, adminId);
            return ResponseEntity.ok(ApiResponse.success("Status updated successfully", status));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
