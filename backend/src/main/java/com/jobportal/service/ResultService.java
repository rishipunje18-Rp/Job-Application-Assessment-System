package com.jobportal.service;

import com.jobportal.model.Result;
import com.jobportal.model.User;
import com.jobportal.repository.ResultRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final UserRepository userRepository;

    public ResultService(ResultRepository resultRepository, UserRepository userRepository) {
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
    }

    public List<Result> getResultsByUserId(Long userId) {
        return resultRepository.findByUserId(userId);
    }

    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }

    public Result getResultBySessionId(Long sessionId) {
        return resultRepository.findByTestSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Result not found for this test session"));
    }

    public void updateResultStatus(Long resultId, String status, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can update result status");
        }
        if (!status.equals("PENDING") && !status.equals("SELECTED") && !status.equals("REJECTED")) {
            throw new RuntimeException("Status must be PENDING, SELECTED, or REJECTED");
        }
        resultRepository.updateStatus(resultId, status);
    }

    public int countResults() {
        return resultRepository.countAll();
    }

    public int countUserResults(Long userId) {
        return resultRepository.countByUserId(userId);
    }
}
