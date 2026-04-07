package com.jobportal.model;

import java.sql.Timestamp;

public class Result {
    private Long id;
    private Long userId;
    private Long testSessionId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private Timestamp completedAt;

    public Result() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTestSessionId() { return testSessionId; }
    public void setTestSessionId(Long testSessionId) { this.testSessionId = testSessionId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}
