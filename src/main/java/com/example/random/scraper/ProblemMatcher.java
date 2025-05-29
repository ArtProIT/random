package com.example.random.scraper;

import com.example.random.model.ProblemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Улучшенный класс для сопоставления названий задач с их информацией
 */
public class ProblemMatcher {
    private static final Logger logger = LoggerFactory.getLogger(ProblemMatcher.class);
    private static final double SIMILARITY_THRESHOLD = 0.8;

    /**
     * Находит информацию о задаче по её названию
     */
    public ProblemInfo findProblemInfo(String solvedTitle, Map<String, ProblemInfo> allProblems) {
        // Точное совпадение
        ProblemInfo exactMatch = allProblems.get(solvedTitle);
        if (exactMatch != null) {
            logger.debug("Exact match found for: {}", solvedTitle);
            return exactMatch;
        }

        ProblemInfo bestMatch = findBestMatch(solvedTitle, allProblems);
        if (bestMatch != null) {
            logger.debug("Best match found for '{}': {}", solvedTitle, bestMatch.getTitle());
        } else {
            logger.warn("No match found for: {}", solvedTitle);
        }

        return bestMatch;
    }


    private ProblemInfo findBestMatch(String solvedTitle, Map<String, ProblemInfo> allProblems) {
        String normalizedSolved = normalizeTitle(solvedTitle);
        ProblemInfo bestMatch = null;
        double bestSimilarity = 0.0;

        for (Map.Entry<String, ProblemInfo> entry : allProblems.entrySet()) {
            String problemTitle = entry.getKey();
            String normalizedProblem = normalizeTitle(problemTitle);

            if (isExactMatch(normalizedSolved, normalizedProblem)) {
                return entry.getValue();
            }

            double similarity = calculateSimilarity(normalizedSolved, normalizedProblem);
            if (similarity > SIMILARITY_THRESHOLD && similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = entry.getValue();
            }
        }

        return bestMatch;
    }

    private boolean isExactMatch(String normalizedSolved, String normalizedProblem) {
        return normalizedProblem.equals(normalizedSolved) ||
                normalizedProblem.contains(normalizedSolved) ||
                normalizedSolved.contains(normalizedProblem);
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }

        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Удаляю специальные символы
                .replaceAll("\\s+", " ")        // Множественные пробелы в один
                .trim();
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return (maxLength - distance) / (double) maxLength;
    }

    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Math.max(s1 != null ? s1.length() : 0, s2 != null ? s2.length() : 0);
        }

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