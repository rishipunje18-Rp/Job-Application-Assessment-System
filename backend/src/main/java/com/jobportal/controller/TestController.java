package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.model.TestSession;
import com.jobportal.service.TestService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTest(@RequestBody Map<String, Object> body, @RequestParam Long adminId) {
        try {
            String title = (String) body.get("title");
            Long jobId = body.get("jobId") != null ? Long.valueOf(body.get("jobId").toString()) : null;
            Long assignedUserId = body.get("assignedUserId") != null ? Long.valueOf(body.get("assignedUserId").toString()) : null;
            
            @SuppressWarnings("unchecked")
            List<Map<String, String>> questionsData = (List<Map<String, String>>) body.get("questions");

            Map<String, Object> response = testService.createTest(title, jobId, assignedUserId, adminId, questionsData);
            return ResponseEntity.ok(ApiResponse.success("Test created and assigned successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/assigned/{userId}")
    public ResponseEntity<ApiResponse<List<TestSession>>> getAssignedTests(@PathVariable Long userId) {
        try {
            List<TestSession> tests = testService.getAssignedTests(userId);
            return ResponseEntity.ok(ApiResponse.success("Assigned tests fetched", tests));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/created/{adminId}")
    public ResponseEntity<ApiResponse<List<TestSession>>> getCreatedTests(@PathVariable Long adminId) {
        List<TestSession> tests = testService.getTestsByCreator(adminId);
        return ResponseEntity.ok(ApiResponse.success("Created tests fetched", tests));
    }
    
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TestSession>>> getAllTests() {
        List<TestSession> tests = testService.getAllTests();
        return ResponseEntity.ok(ApiResponse.success("All tests fetched", tests));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTest(@RequestBody Map<String, Long> body) {
        try {
            Long userId = body.get("userId");
            Long sessionId = body.get("sessionId");
            Map<String, Object> testData = testService.startTest(sessionId, userId);
            return ResponseEntity.ok(ApiResponse.success("Test started", testData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

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
