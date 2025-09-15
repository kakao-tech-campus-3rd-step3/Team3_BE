package com.shootdoori.match.exception;

public class TeamCapacityExceededException extends RuntimeException {

    public TeamCapacityExceededException(String message) {
        super(message);
    }
}
