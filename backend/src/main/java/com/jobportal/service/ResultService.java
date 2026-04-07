package com.jobportal.service;

import com.jobportal.model.Result;
import com.jobportal.repository.ResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultService {

    private final ResultRepository resultRepository;

    public ResultService(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    public List<Result> getResultsByUserId(Long userId) {
        return resultRepository.findByUserId(userId);
    }

    public Result getResultBySessionId(Long sessionId) {
        return resultRepository.findByTestSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Result not found for this test session"));
    }
}
