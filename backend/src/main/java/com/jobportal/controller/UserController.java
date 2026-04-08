package com.jobportal.controller;

import com.jobportal.model.User;
import com.jobportal.service.UserService;
import com.jobportal.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// =============================================
// USER CONTROLLER — Handles registration, login, and user lookups
// =============================================
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor injection (Spring auto-wires this)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users/register — Register a new user (ADMIN or STUDENT)
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        try {
            User saved = userService.register(user);
            // Don't send password back to client
            saved.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("Registration successful", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/users/login — Login with email + password
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            User user = userService.login(email, password);
            // Don't send password back to client
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("Login successful", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/users/{id} — Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(ApiResponse.success("User found", user));
                })
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("User not found")));
    }

    // GET /api/users/students — List all students (Admin uses this for test assignment dropdown)
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getStudents() {
        List<User> students = userService.findStudents();
        // Remove passwords before sending
        students.forEach(s -> s.setPassword(null));
        return ResponseEntity.ok(ApiResponse.success("Students fetched", students));
    }
}
