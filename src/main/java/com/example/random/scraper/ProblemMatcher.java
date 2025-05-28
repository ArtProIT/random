package com.example.random.scraper;

import java.util.Map;

/**
 * Класс для сопоставления названий задач с их номерами
 */
public class ProblemMatcher {
    private static final double SIMILARITY_THRESHOLD = 0.8;

    /**
     * Находит номер задачи по её названию
     */
    public Integer findProblemNumber(String solvedTitle, Map<String, Integer> allProblems) {
        // Точное совпадение
        Integer exactMatch = allProblems.get(solvedTitle);
        if (exactMatch != null) {
            return exactMatch;
        }
        return findBestMatch(solvedTitle, allProblems);
    }

    private Integer findBestMatch(String solvedTitle, Map<String, Integer> allProblems) {
        String normalizedSolved = normalizeTitle(solvedTitle);

        for (Map.Entry<String, Integer> entry : allProblems.entrySet()) {
            String problemTitle = entry.getKey();
            String normalizedProblem = normalizeTitle(problemTitle);

            if (isMatchingTitle(normalizedSolved, normalizedProblem)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private boolean isMatchingTitle(String normalizedSolved, String normalizedProblem) {
        return normalizedProblem.equals(normalizedSolved) ||
                normalizedProblem.contains(normalizedSolved) ||
                normalizedSolved.contains(normalizedProblem) ||
                calculateSimilarity(normalizedSolved, normalizedProblem) > SIMILARITY_THRESHOLD;
    }

    private String normalizeTitle(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") //Удаляю специальные символы
                .replaceAll("\\s+", " ")        //Множественные пробелы в один
                .trim();
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;

        return (maxLength - levenshteinDistance(s1, s2)) / (double) maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }
}