package com.shootdoori.match.entity.common;

import com.shootdoori.match.entity.user.UserStatus;
import com.shootdoori.match.exception.common.DuplicatedException;
import com.shootdoori.match.exception.common.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class SoftDeleteUserInfo {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    public UserStatus getStatus() {
        return status;
    }

    public void changeStatusDeleted() {

        if (isDeleted()) {
            throw new DuplicatedException(ErrorCode.USER_ALREADY_DELETED);
        }

        status = UserStatus.DELETED;
    }

    public boolean isDeleted() {
        return status == UserStatus.DELETED;
    }

    public void changeStatusActive() {
        if (isActive()) {
            throw new DuplicatedException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        status = UserStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}
