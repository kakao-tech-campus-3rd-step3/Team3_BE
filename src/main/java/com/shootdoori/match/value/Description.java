package com.shootdoori.match.value;

import jakarta.persistence.Embeddable;

@Embeddable
public record Description(String description) {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public Description {
        if (description != null && description.isBlank()) {
            description = null;
        }

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("설명은 최대 1000자입니다.");
        }
    }

    public static Description of(String description) {
        return new Description(description);
    }

    public static Description empty() {
        return new Description(null);
    }
}
