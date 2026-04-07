package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.service.ResultService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    // GET /api/results/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Result>>> getResultsByUser(@PathVariable Long userId) {
        List<Result> results = resultService.getResultsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Results fetched", results));
    }

    // GET /api/results/session/{sessionId}
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<Result>> getResultBySession(@PathVariable Long sessionId) {
        try {
            Result result = resultService.getResultBySessionId(sessionId);
            return ResponseEntity.ok(ApiResponse.success("Result found", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
