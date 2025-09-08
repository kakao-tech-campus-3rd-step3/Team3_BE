package com.shootdoori.match.entity;

public enum TeamType {
    CENTRAL_CLUB("중앙동아리"),
    DEPARTMENT_CLUB("과동아리"), 
    OTHER("기타");
    
    private final String displayName;
    
    TeamType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static TeamType fromDisplayName(String displayName) {
        for (TeamType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown team type: " + displayName);
    }
}
