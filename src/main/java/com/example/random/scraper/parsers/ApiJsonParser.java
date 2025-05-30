package com.example.random.scraper.parsers;

import com.example.random.model.ProblemDifficulty;
import com.example.random.model.ProblemInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Парсер JSON данных из LeetCode API
 */
@Slf4j
public class ApiJsonParser {
    private Consumer<String> progressCallback;

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        log.info(message);
    }

    /**
     * Парсит JSON и возвращает мапу с информацией о задачах
     */
    public Map<String, ProblemInfo> parseProblemsFromJson(String json) {
        Map<String, ProblemInfo> allProblems = new HashMap<>();

        try {
            logProgress("Начинаем парсинг JSON данных...");

            JSONObject root = new JSONObject(json);

            if (!root.has("stat_status_pairs")) {
                throw new IllegalArgumentException("JSON не содержит поле 'stat_status_pairs'");
            }

            JSONArray problems = root.getJSONArray("stat_status_pairs");
            logProgress("Найдено задач в JSON: " + problems.length());

            int successfullyParsed = 0;
            int errors = 0;

            for (int i = 0; i < problems.length(); i++) {
                try {
                    ProblemInfo problemInfo = parseSingleProblem(problems.getJSONObject(i));
                    if (problemInfo != null) {
                        allProblems.put(problemInfo.getTitle(), problemInfo);
                        successfullyParsed++;
                    }
                } catch (Exception e) {
                    errors++;
                    log.debug("Error parsing problem at index {}: {}", i, e.getMessage());
                }
            }

            logProgress("Успешно обработано задач: " + successfullyParsed);
            if (errors > 0) {
                logProgress("Ошибок при парсинге: " + errors);
            }

        } catch (Exception e) {
            String errorMsg = "Критическая ошибка парсинга JSON: " + e.getMessage();
            logProgress(errorMsg);
            log.error("JSON parsing error", e);
            throw new RuntimeException(errorMsg, e);
        }

        return allProblems;
    }

    /**
     * Парсит одну задачу из JSON объекта
     */
    private ProblemInfo parseSingleProblem(JSONObject problemJson) {
        try {
            JSONObject stat = problemJson.getJSONObject("stat");
            JSONObject difficulty = problemJson.getJSONObject("difficulty");

            int number = stat.getInt("frontend_question_id");
            String title = stat.getString("question__title").trim();
            int level = difficulty.getInt("level");

            // Валидация данных
            if (number <= 0 || title.isEmpty() || level < 1 || level > 3) {
                log.debug("Invalid problem data: number={}, title='{}', level={}", number, title, level);
                return null;
            }

            ProblemDifficulty problemDifficulty = ProblemDifficulty.fromLevel(level);
            return new ProblemInfo(number, title, problemDifficulty);

        } catch (Exception e) {
            log.debug("Error parsing single problem: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Валидирует JSON перед парсингом
     */
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            JSONObject root = new JSONObject(json);
            return root.has("stat_status_pairs") &&
                    root.getJSONArray("stat_status_pairs").length() > 0;
        } catch (Exception e) {
            log.debug("JSON validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получает статистику по уровням сложности из JSON
     */
    public Map<ProblemDifficulty, Integer> getStatistics(String json) {
        Map<ProblemDifficulty, Integer> stats = new HashMap<>();
        stats.put(ProblemDifficulty.EASY, 0);
        stats.put(ProblemDifficulty.MEDIUM, 0);
        stats.put(ProblemDifficulty.HARD, 0);

        try {
            JSONObject root = new JSONObject(json);
            JSONArray problems = root.getJSONArray("stat_status_pairs");

            for (int i = 0; i < problems.length(); i++) {
                try {
                    JSONObject difficulty = problems.getJSONObject(i).getJSONObject("difficulty");
                    int level = difficulty.getInt("level");
                    ProblemDifficulty problemDifficulty = ProblemDifficulty.fromLevel(level);
                    stats.put(problemDifficulty, stats.get(problemDifficulty) + 1);
                } catch (Exception e) {
                    log.debug("Error getting stats for problem {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error calculating statistics", e);
        }

        return stats;
    }
}