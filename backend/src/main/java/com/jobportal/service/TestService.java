package com.jobportal.service;

import com.jobportal.model.Question;
import com.jobportal.model.Result;
import com.jobportal.model.TestSession;
import com.jobportal.repository.QuestionRepository;
import com.jobportal.repository.ResultRepository;
import com.jobportal.repository.TestSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final QuestionRepository questionRepository;
    private final TestSessionRepository testSessionRepository;
    private final ResultRepository resultRepository;

    public TestService(QuestionRepository questionRepository,
                       TestSessionRepository testSessionRepository,
                       ResultRepository resultRepository) {
        this.questionRepository = questionRepository;
        this.testSessionRepository = testSessionRepository;
        this.resultRepository = resultRepository;
    }

    /**
     * Start a new test:
     * 1. Fetch 10 random questions
     * 2. Create a test session
     * 3. Return questions (without correct answers)
     */
    public Map<String, Object> startTest(Long userId) {
        // Fetch 10 random questions
        List<Question> questions = questionRepository.findRandomQuestions(10);

        if (questions.size() < 10) {
            throw new RuntimeException("Not enough questions in the bank. Need at least 10.");
        }

        // Build comma-separated question IDs
        String questionIds = questions.stream()
                .map(q -> String.valueOf(q.getId()))
                .collect(Collectors.joining(","));

        // Create test session
        TestSession session = new TestSession();
        session.setUserId(userId);
        session.setQuestionIds(questionIds);
        testSessionRepository.save(session);

        // Remove correct answers before sending to client
        List<Map<String, Object>> questionList = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("optionA", q.getOptionA());
            qMap.put("optionB", q.getOptionB());
            qMap.put("optionC", q.getOptionC());
            qMap.put("optionD", q.getOptionD());
            questionList.add(qMap);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", session.getId());
        response.put("totalQuestions", questions.size());
        response.put("questions", questionList);

        return response;
    }

    /**
     * Submit test answers:
     * 1. Validate session
     * 2. Calculate score
     * 3. Store result
     */
    public Result submitTest(Long sessionId, Long userId, Map<String, String> answers) {
        // Validate session
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Test session not found"));

        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("This test session doesn't belong to you");
        }

        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("This test has already been submitted");
        }

        // Fetch questions for this session
        List<Long> questionIds = Arrays.stream(session.getQuestionIds().split(","))
                .map(Long::parseLong)
                .toList();

        List<Question> questions = questionRepository.findByIds(questionIds);

        // Calculate score
        int score = 0;
        int totalQuestions = questions.size();

        for (Question q : questions) {
            String userAnswer = answers.get(String.valueOf(q.getId()));
            if (userAnswer != null && userAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {
                score++;
            }
        }

        double percentage = (totalQuestions > 0) ? ((double) score / totalQuestions) * 100 : 0;

        // Mark session completed
        testSessionRepository.markCompleted(sessionId);

        // Save result
        Result result = new Result();
        result.setUserId(userId);
        result.setTestSessionId(sessionId);
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);
        result.setPercentage(Math.round(percentage * 100.0) / 100.0);

        return resultRepository.save(result);
    }
}
