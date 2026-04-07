package com.jobportal.model;

import java.sql.Timestamp;

public class Application {
    private Long id;
    private Long userId;
    private Long jobId;
    private String status;
    private Timestamp appliedAt;

    // Extra fields for display
    private String userName;
    private String jobTitle;

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
}
