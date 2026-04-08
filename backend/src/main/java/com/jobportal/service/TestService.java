package com.jobportal.service;

import com.jobportal.model.Question;
import com.jobportal.model.Result;
import com.jobportal.model.TestSession;
import com.jobportal.model.User;
import com.jobportal.repository.QuestionRepository;
import com.jobportal.repository.ResultRepository;
import com.jobportal.repository.TestSessionRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final QuestionRepository questionRepository;
    private final TestSessionRepository testSessionRepository;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;

    public TestService(QuestionRepository questionRepository,
                       TestSessionRepository testSessionRepository,
                       ResultRepository resultRepository,
                       UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.testSessionRepository = testSessionRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
    }

    /**
     * Admin creates a test with custom MCQ questions and assigns to a job/student.
     */
    public Map<String, Object> createTest(String title, Long jobId, Long assignedUserId, Long createdBy,
                                           List<Map<String, String>> questionsData) {
        User admin = userRepository.findById(createdBy)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equals(admin.getRole())) {
            throw new RuntimeException("Only admins can create tests");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Test title is required");
        }
        if (questionsData == null || questionsData.isEmpty()) {
            throw new RuntimeException("At least one question is required");
        }

        // Create test session
        TestSession session = new TestSession();
        session.setTitle(title);
        session.setJobId(jobId);
        session.setCreatedBy(createdBy);
        session.setAssignedUserId(assignedUserId);
        session.setUserId(assignedUserId);
        session.setStatus("ASSIGNED");
        session.setQuestionIds(""); // will update after saving questions
        testSessionRepository.save(session);

        // Save questions linked to this test session
        List<Long> questionIds = new ArrayList<>();
        for (Map<String, String> qData : questionsData) {
            Question q = new Question();
            q.setTestSessionId(session.getId());
            q.setQuestionText(qData.get("questionText"));
            q.setOptionA(qData.get("optionA"));
            q.setOptionB(qData.get("optionB"));
            q.setOptionC(qData.get("optionC"));
            q.setOptionD(qData.get("optionD"));
            q.setCorrectAnswer(qData.get("correctAnswer"));
            questionRepository.save(q);
            questionIds.add(q.getId());
        }

        // Update question_ids in session
        String qIds = questionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        session.setQuestionIds(qIds);
        // Persist the question IDs to the database
        updateSessionQuestionIds(session.getId(), qIds);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", session.getId());
        response.put("title", title);
        response.put("totalQuestions", questionIds.size());
        response.put("assignedUserId", assignedUserId);
        response.put("jobId", jobId);

        return response;
    }

    // Update the question_ids column in the test session after saving questions
    private void updateSessionQuestionIds(Long sessionId, String questionIds) {
        testSessionRepository.updateQuestionIds(sessionId, questionIds);
    }

    /**
     * Get tests assigned to a specific student.
     */
    public List<TestSession> getAssignedTests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"STUDENT".equals(user.getRole())) {
            throw new RuntimeException("Only students can view assigned tests");
        }
        return testSessionRepository.findByAssignedUserId(userId);
    }

    /**
     * Get tests created by an admin.
     */
    public List<TestSession> getTestsByCreator(Long adminId) {
        return testSessionRepository.findByCreatedBy(adminId);
    }

    /**
     * Get all tests (admin).
     */
    public List<TestSession> getAllTests() {
        return testSessionRepository.findAll();
    }

    /**
     * Student starts an assigned test — fetches questions without correct answers.
     */
    public Map<String, Object> startTest(Long sessionId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"STUDENT".equals(user.getRole())) {
            throw new RuntimeException("Only students can take tests");
        }

        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Test session not found"));

        if (!session.getAssignedUserId().equals(userId)) {
            throw new RuntimeException("This test is not assigned to you");
        }

        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("This test has already been completed");
        }

        // Mark as in progress
        testSessionRepository.markInProgress(sessionId);

        // Fetch questions for this session
        List<Question> questions;
        if (session.getQuestionIds() != null && !session.getQuestionIds().isEmpty()) {
            List<Long> questionIds = Arrays.stream(session.getQuestionIds().split(","))
                    .map(Long::parseLong)
                    .toList();
            questions = questionRepository.findByIds(questionIds);
        } else {
            // Fall back to test_session_id linked questions
            questions = questionRepository.findByTestSessionId(sessionId);
        }

        if (questions.isEmpty()) {
            throw new RuntimeException("No questions found for this test");
        }

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
        response.put("title", session.getTitle());
        response.put("totalQuestions", questions.size());
        response.put("questions", questionList);

        return response;
    }

    /**
     * Submit test answers — calculate score, store result.
     */
    public Result submitTest(Long sessionId, Long userId, Map<String, String> answers) {
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Test session not found"));

        if (session.getAssignedUserId() != null && !session.getAssignedUserId().equals(userId)) {
            throw new RuntimeException("This test session doesn't belong to you");
        }

        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("This test has already been submitted");
        }

        // Fetch questions for this session
        List<Question> questions;
        if (session.getQuestionIds() != null && !session.getQuestionIds().isEmpty()) {
            List<Long> questionIds = Arrays.stream(session.getQuestionIds().split(","))
                    .map(Long::parseLong)
                    .toList();
            questions = questionRepository.findByIds(questionIds);
        } else {
            questions = questionRepository.findByTestSessionId(sessionId);
        }

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
        result.setStatus("PENDING");

        return resultRepository.save(result);
    }

    // ── Stats ──
    public int countTestsByCreator(Long adminId) {
        return testSessionRepository.countByCreatedBy(adminId);
    }

    public int countAssignedTests(Long userId) {
        return testSessionRepository.countByAssignedUserId(userId);
    }

    public int countAllTests() {
        return testSessionRepository.countAll();
    }
}
