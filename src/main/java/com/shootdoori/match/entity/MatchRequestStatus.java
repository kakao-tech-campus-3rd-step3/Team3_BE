package com.shootdoori.match.entity;

public enum MatchApplicationStatus {
  PENDING("대기중"),
  ACCEPTED("수락"),
  REJECTED("거절"),
  CANCELED("취소");

  private final String displayName;

  MatchApplicationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static MatchApplicationStatus fromDisplayName(String displayName) {
    for (MatchApplicationStatus status : values()) {
      if (status.displayName.equals(displayName)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown match application status: " + displayName);
  }
}
