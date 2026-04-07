package com.jobportal.model;

import java.sql.Timestamp;

public class TestSession {
    private Long id;
    private Long userId;
    private String questionIds; // comma-separated question IDs
    private String status;      // IN_PROGRESS or COMPLETED
    private Timestamp startedAt;
    private Timestamp completedAt;

    public TestSession() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getQuestionIds() { return questionIds; }
    public void setQuestionIds(String questionIds) { this.questionIds = questionIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
}
