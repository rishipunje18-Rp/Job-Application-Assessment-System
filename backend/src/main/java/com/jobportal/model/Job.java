package com.jobportal.model;

import java.sql.Timestamp;

public class Job {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private String salary;
    private Long createdBy;
    private Timestamp createdAt;

    public Job() {}

    public Job(String title, String description, String company, String location, String salary, Long createdBy) {
        this.title = title;
        this.description = description;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
