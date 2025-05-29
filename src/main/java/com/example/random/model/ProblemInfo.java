package com.example.random.model;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Класс для хранения информации о задаче
 */
@Value
@EqualsAndHashCode(of = "number")
public class ProblemInfo {
    int number;
    String title;
    ProblemDifficulty difficulty;

    @Override
    public String toString() {
        return String.format("Problem{number=%d, title='%s', difficulty=%s}",
                number, title, difficulty);
    }
}