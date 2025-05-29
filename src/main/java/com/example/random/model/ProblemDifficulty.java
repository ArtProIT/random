package com.example.random.model;

/**
 * Enum для уровней сложности задач LeetCode
 */
public enum ProblemDifficulty {
    EASY(1, "Easy"),
    MEDIUM(2, "Medium"),
    HARD(3, "Hard");

    private final int level;
    private final String displayName;

    ProblemDifficulty(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ProblemDifficulty fromLevel(int level) {
        for (ProblemDifficulty difficulty : values()) {
            if (difficulty.level == level) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty level: " + level);
    }

    @Override
    public String toString() {
        return displayName;
    }
}