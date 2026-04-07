package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.service.TestService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    // POST /api/test/start — Start a new test
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTest(@RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            Map<String, Object> testData = testService.startTest(userId);
            return ResponseEntity.ok(ApiResponse.success("Test started", testData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/test/submit — Submit test answers
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Result>> submitTest(@RequestBody Map<String, Object> body) {
        try {
            Long sessionId = Long.valueOf(body.get("sessionId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());

            @SuppressWarnings("unchecked")
            Map<String, String> answers = (Map<String, String>) body.get("answers");

            Result result = testService.submitTest(sessionId, userId, answers);
            return ResponseEntity.ok(ApiResponse.success("Test submitted successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
