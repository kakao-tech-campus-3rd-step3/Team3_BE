package com.shootdoori.match.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "universityEmail")
    }
)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(nullable = false, length = 255)
  private String universityEmail;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 20)
  private String phoneNumber;

  @Column(nullable = false, length = 100)
  private String university;

  @Column(nullable = false, length = 100)
  private String department;

  @Column(nullable = false, length = 2)
  private String studentYear;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  protected User() {}

  public User(Long userId, String email, String universityEmail, String password,
      String name, String phoneNumber, String university, String department,
      String studentYear, String bio, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.userId = userId;
    this.email = email;
    this.universityEmail = universityEmail;
    this.password = password;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.university = university;
    this.department = department;
    this.studentYear = studentYear;
    this.bio = bio;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getUniversityEmail() {
    return universityEmail;
  }

  public String getPassword() {
    return password;
  }

  public String getName() {
    return name;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getUniversity() {
    return university;
  }

  public String getDepartment() {
    return department;
  }

  public String getStudentYear() {
    return studentYear;
  }

  public String getBio() {
    return bio;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
