package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.service.JobService;
import com.jobportal.service.ResultService;
import com.jobportal.service.TestService;
import com.jobportal.service.UserService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JobService jobService;
    private final TestService testService;
    private final ResultService resultService;

    public UserController(UserService userService, JobService jobService, TestService testService, ResultService resultService) {
        this.userService = userService;
        this.jobService = jobService;
        this.testService = testService;
        this.resultService = resultService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        try {
            User saved = userService.register(user);
            saved.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            User user = userService.login(email, password);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("Login successful", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(ApiResponse.success("User found", user));
                })
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("User not found")));
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getStudents() {
        List<User> students = userService.findStudents();
        students.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success("Students fetched", students));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getStats(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Map<String, Integer> stats = new HashMap<>();

            if ("ADMIN".equals(user.getRole())) {
                stats.put("totalJobs", jobService.countJobs());
                stats.put("totalApplicants", jobService.countApplications());
                stats.put("testsCreated", testService.countTestsByCreator(userId));
                stats.put("totalResults", resultService.countResults());
                stats.put("totalStudents", userService.countStudents());
            } else {
                stats.put("availableJobs", jobService.countJobs());
                stats.put("applications", jobService.countUserApplications(userId));
                stats.put("testsAssigned", testService.countAssignedTests(userId));
                stats.put("testsTaken", resultService.countUserResults(userId));
            }

            return ResponseEntity.ok(ApiResponse.success("Stats fetched", stats));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
