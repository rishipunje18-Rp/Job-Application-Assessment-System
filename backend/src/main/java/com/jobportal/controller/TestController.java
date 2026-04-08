package com.jobportal.controller;

import com.jobportal.model.Result;
import com.jobportal.model.TestSession;
import com.jobportal.service.TestService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// =============================================
// TEST CONTROLLER — Handles test creation, assignment, and submission
// Admin: create tests with MCQ questions, assign to students, view all tests
// Student: view assigned tests, start test, submit answers
// =============================================
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    // ── ADMIN ENDPOINTS ─────────────────────────────

    // POST /api/test/create — Admin creates a test with custom MCQ questions
    // Request body: { title, jobId (optional), assignedUserId, createdBy, questions: [...] }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTest(@RequestBody Map<String, Object> body) {
        try {
            String title = (String) body.get("title");

            // jobId is optional — test can be assigned without linking to a job
            Long jobId = null;
            if (body.get("jobId") != null && !body.get("jobId").toString().isEmpty()) {
                jobId = Long.valueOf(body.get("jobId").toString());
            }

            Long assignedUserId = Long.valueOf(body.get("assignedUserId").toString());
            Long createdBy = Long.valueOf(body.get("createdBy").toString());

            // Extract the list of question maps from the request
            @SuppressWarnings("unchecked")
            List<Map<String, String>> questions = (List<Map<String, String>>) body.get("questions");

            Map<String, Object> result = testService.createTest(title, jobId, assignedUserId, createdBy, questions);
            return ResponseEntity.ok(ApiResponse.success("Test created successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/test/all — Admin views all tests
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TestSession>>> getAllTests() {
        List<TestSession> tests = testService.getAllTests();
        return ResponseEntity.ok(ApiResponse.success("All tests fetched", tests));
    }

    // ── STUDENT ENDPOINTS ───────────────────────────

    // GET /api/test/assigned/{userId} — Student views their assigned tests
    @GetMapping("/assigned/{userId}")
    public ResponseEntity<ApiResponse<List<TestSession>>> getAssignedTests(@PathVariable Long userId) {
        try {
            List<TestSession> tests = testService.getAssignedTests(userId);
            return ResponseEntity.ok(ApiResponse.success("Assigned tests fetched", tests));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/test/start — Student starts an assigned test
    // Request body: { sessionId, userId }
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> startTest(@RequestBody Map<String, Long> body) {
        try {
            Long sessionId = body.get("sessionId");
            Long userId = body.get("userId");
            // This returns questions WITHOUT correct answers (safe for client)
            Map<String, Object> testData = testService.startTest(sessionId, userId);
            return ResponseEntity.ok(ApiResponse.success("Test started", testData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/test/submit — Student submits test answers
    // Request body: { sessionId, userId, answers: { "questionId": "A/B/C/D" } }
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Result>> submitTest(@RequestBody Map<String, Object> body) {
        try {
            Long sessionId = Long.valueOf(body.get("sessionId").toString());
            Long userId = Long.valueOf(body.get("userId").toString());

            // Extract answer map: { "1": "A", "2": "C", ... }
            @SuppressWarnings("unchecked")
            Map<String, String> answers = (Map<String, String>) body.get("answers");

            Result result = testService.submitTest(sessionId, userId, answers);
            return ResponseEntity.ok(ApiResponse.success("Test submitted successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
