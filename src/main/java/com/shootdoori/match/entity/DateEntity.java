package com.shootdoori.match.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class DateEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedDate
    private LocalDateTime statusChangedAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt()  {
        return updatedAt;
    }

    public UserStatus getUserStatus() {
        return status;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void requestDeletion() {
        this.status = UserStatus.PENDING_DELETION;
    }
}
