package com.example.random.scraper;

import com.example.random.model.ProblemInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Улучшенный класс для сопоставления названий задач с их номерами
 */
@Slf4j
public class ProblemMatcher {

    /**
     * Находит ProblemInfo по названию задачи
     */
    public ProblemInfo findProblemInfo(String solvedTitle, Map<String, ProblemInfo> allProblems) {
        if (solvedTitle == null || solvedTitle.trim().isEmpty()) {
            return null;
        }

        String cleanTitle = solvedTitle.trim();

        // Точное совпадение
        ProblemInfo exactMatch = allProblems.get(cleanTitle);
        if (exactMatch != null) {
            log.debug("Exact match found for: {}", cleanTitle);
            return exactMatch;
        }

        // Поиск с игнорированием регистра
        for (Map.Entry<String, ProblemInfo> entry : allProblems.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cleanTitle)) {
                log.debug("Case-insensitive match found for: {}", cleanTitle);
                return entry.getValue();
            }
        }

        // Поиск по частичному совпадению
        for (Map.Entry<String, ProblemInfo> entry : allProblems.entrySet()) {
            String apiTitle = entry.getKey();

            if (isPartialMatch(cleanTitle, apiTitle)) {
                log.debug("Partial match found: '{}' -> '{}'", cleanTitle, apiTitle);
                return entry.getValue();
            }
        }

        log.debug("No match found for: {}", cleanTitle);
        return null;
    }

    /**
     * Сопоставляет множество названий задач с их номерами
     */
    public Set<Integer> matchProblemsToNumbers(Set<String> solvedTitles, Map<String, ProblemInfo> allProblems) {
        Set<Integer> solvedNumbers = new HashSet<>();

        int matchedCount = 0;
        for (String solvedTitle : solvedTitles) {
            ProblemInfo problemInfo = findProblemInfo(solvedTitle, allProblems);
            if (problemInfo != null) {
                solvedNumbers.add(problemInfo.getNumber());
                matchedCount++;
            }
        }

        log.info("Сопоставлено задач: {} из {}", matchedCount, solvedTitles.size());
        return solvedNumbers;
    }

    /**
     * Проверяет частичное совпадение названий
     */
    private boolean isPartialMatch(String solvedTitle, String apiTitle) {
        // Нормализуем строки
        String normalizedSolved = normalizeTitle(solvedTitle);
        String normalizedApi = normalizeTitle(apiTitle);

        // Проверяем различные варианты совпадений
        return normalizedSolved.equals(normalizedApi) ||
                normalizedApi.contains(normalizedSolved) ||
                normalizedSolved.contains(normalizedApi) ||
                calculateSimilarity(normalizedSolved, normalizedApi) > 0.8;
    }

    /**
     * Нормализует название задачи для сравнения
     */
    private String normalizeTitle(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Вычисляет схожесть строк (простой алгоритм)
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }

        return (maxLen - calculateLevenshteinDistance(s1, s2)) / (double) maxLen;
    }

    /**
     * Вычисляет расстояние Левенштейна
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i-1][j] + 1, dp[i][j-1] + 1),
                            dp[i-1][j-1] + (s1.charAt(i-1) == s2.charAt(j-1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}