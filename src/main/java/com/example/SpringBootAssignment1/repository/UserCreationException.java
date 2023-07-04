package com.example.SpringBootAssignment1.repository;



public class UserCreationException extends Exception {
    private final int createdUserCount;
    private final int duplicateUserCount;

    public UserCreationException(int createdUserCount, int duplicateUserCount) {
        this.createdUserCount = createdUserCount;
        this.duplicateUserCount = duplicateUserCount;
    }

    public int getCreatedUserCount() {
        return createdUserCount;
    }

    public int getDuplicateUserCount() {
        return duplicateUserCount;
    }
}
