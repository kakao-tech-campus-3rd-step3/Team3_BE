package com.shootdoori.match.entity;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "university_email",nullable = false, unique = true)
    private String universityEmail;

    @Column(name = "phone_number",nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String university;

    @Column(nullable = false)
    private String department;

    @Column(name = "student_year",nullable = false)
    private String studentYear;

    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected User() {

    }

    public User(ProfileCreateRequest createRequest) {
        this.name = createRequest.name();
        this.email = createRequest.email();
        this.universityEmail = createRequest.universityEmail();
        this.phoneNumber = createRequest.phoneNumber();
        this.university = createRequest.university();
        this.department = createRequest.department();
        this.studentYear = createRequest.studentYear();
        this.bio = createRequest.bio();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUniversityEmail() {
        return this.universityEmail;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getUniversity() {
        return this.university;
    }

    public String getDepartment() {
        return this.department;
    }

    public String getStudentYear() {
        return this.studentYear;
    }

    public String getBio() {
        return this.bio;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void update(ProfileUpdateRequest updateRequest) {
        this.name = updateRequest.name();
    }
}
