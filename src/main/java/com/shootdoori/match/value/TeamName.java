package com.shootdoori.match.value;

import jakarta.persistence.Embeddable;

@Embeddable
public record TeamName(String name) {

    private static final int MAX_TEAM_NAME_LENGTH = 100;

    public TeamName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다.");
        }
        if (name.length() > MAX_TEAM_NAME_LENGTH) {
            throw new IllegalArgumentException("팀 이름은 최대 100자입니다.");
        }
    }

    public static TeamName of(String name) {
        return new TeamName(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TeamName that = (TeamName) other;
        return this.name.equals(that.name());
    }
}
