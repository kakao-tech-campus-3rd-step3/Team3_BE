package com.shootdoori.match.entity.mercenary;

public enum RecruitmentStatus {
    RECRUITING("모집중"),
    RECRUITMENT_COMPLETED("모집완료"),
    RECRUITMENT_CANCELLED("모집취소");

    private final String displayName;

    RecruitmentStatus(String description) {
        this.displayName = description;
    }

    public static RecruitmentStatus fromDisplayName(String displayName) {
        for (RecruitmentStatus type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown Recruitment status: " + displayName);
    }

    public static RecruitmentStatus fromCode(String code) {
        try {
            return RecruitmentStatus.valueOf(code.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Recruitment status code: " + code);
        }
    }

    public String getDisplayName() {
        return displayName;
    }
}
