package com.example.random.model;

/**
 * Класс для хранения информации о задаче
 */
public class ProblemInfo {
    private final int number;
    private final String title;
    private final ProblemDifficulty difficulty;

    public ProblemInfo(int number, String title, ProblemDifficulty difficulty) {
        this.number = number;
        this.title = title;
        this.difficulty = difficulty;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public ProblemDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemInfo that = (ProblemInfo) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }

    @Override
    public String toString() {
        return String.format("Problem{number=%d, title='%s', difficulty=%s}",
                number, title, difficulty);
    }
}