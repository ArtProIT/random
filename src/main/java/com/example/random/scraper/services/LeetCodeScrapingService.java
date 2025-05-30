package com.example.random.scraper.services;

import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemInfo;
import com.example.random.scraper.browser.BrowserManager;
import com.example.random.scraper.browser.RetryManager;
import com.example.random.scraper.pages.LeetCodeApiPage;
import com.example.random.scraper.pages.LeetCodeProfilePage;
import com.example.random.scraper.parsers.ApiJsonParser;
import com.example.random.scraper.ProblemMatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Главный сервис для скрапинга данных с LeetCode
 * Координирует работу всех компонентов системы
 */
@Slf4j
public class LeetCodeScrapingService {
    private final CacheService cacheService;
    private final RetryManager retryManager;
    private final ApiJsonParser jsonParser;
    private final ProblemMatcher problemMatcher;

    private Consumer<String> progressCallback;

    public LeetCodeScrapingService() {
        this.cacheService = new CacheService();
        this.retryManager = new RetryManager();
        this.jsonParser = new ApiJsonParser();
        this.problemMatcher = new ProblemMatcher();
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;

        // Передаем callback всем компонентам
        cacheService.setProgressCallback(callback);
        retryManager.setProgressCallback(callback);
        jsonParser.setProgressCallback(callback);
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        log.info(message);
    }

    /**
     * Получает номера решенных задач для пользователя
     */
    public Set<Integer> fetchSolvedProblems(String username, boolean headless) throws LeetCodeScrapingException {
        logProgress("Начинаем получение решенных задач для пользователя: " + username);
        logProgress("Режим браузера: " + (headless ? "headless (скрытый)" : "обычный (видимый)"));

        try (BrowserManager browserManager = new BrowserManager(headless)) {
            browserManager.setProgressCallback(progressCallback);

            // Получаем названия решенных задач
            Set<String> solvedTitles = getSolvedProblemsFromProfile(browserManager, username);

            if (solvedTitles.isEmpty()) {
                logProgress("Решенные задачи не найдены");
                return Set.of();
            }

            // Получаем все задачи с номерами
            Map<String, ProblemInfo> allProblems = getAllProblemsInfo(browserManager);

            // Сопоставляем названия с номерами
            Set<Integer> solvedNumbers = matchSolvedProblems(solvedTitles, allProblems);

            logProgress("Процесс завершен успешно!");
            return solvedNumbers;

        } catch (Exception e) {
            String errorMsg = "Ошибка при получении решенных задач: " + e.getMessage();
            logProgress(errorMsg);
            throw new LeetCodeScrapingException(errorMsg, e);
        }
    }

    /**
     * Получает информацию о всех задачах с проверкой кэша
     */
    public Map<String, ProblemInfo> getAllProblemsInfo(boolean headless) throws ApiDataException {
        // Проверяем кэш
        Map<String, ProblemInfo> cachedProblems = cacheService.getCachedProblems();
        if (cachedProblems != null) {
            return cachedProblems;
        }

        logProgress("Режим браузера: " + (headless ? "headless (скрытый)" : "обычный (видимый)"));

        try (BrowserManager browserManager = new BrowserManager(headless)) {
            browserManager.setProgressCallback(progressCallback);
            Map<String, ProblemInfo> problems = getAllProblemsInfo(browserManager);

            // Сохраняем в кэш
            cacheService.cacheProblems(problems);

            return problems;

        } catch (Exception e) {
            throw new ApiDataException("Ошибка при получении информации о задачах", e);
        }
    }

    /**
     * Получает решенные задачи из профиля с повторными попытками
     */
    private Set<String> getSolvedProblemsFromProfile(BrowserManager browserManager, String username)
            throws LeetCodeScrapingException {

        return retryManager.executeWithRetry(
                () -> {
                    LeetCodeProfilePage profilePage = new LeetCodeProfilePage(browserManager.createPage());
                    profilePage.setProgressCallback(progressCallback);

                    profilePage.openProfile(username);
                    profilePage.openRecentAC(); // Может не сработать, но это нормально

                    return profilePage.extractSolvedProblems();
                },
                "получение решенных задач из профиля",
                result -> result != null && !result.isEmpty()
        );
    }

    /**
     * Получает все задачи из API
     */
    private Map<String, ProblemInfo> getAllProblemsInfo(BrowserManager browserManager) throws Exception {
        logProgress("Получаем полный список задач через API...");

        LeetCodeApiPage apiPage = new LeetCodeApiPage(browserManager.createPage());
        apiPage.setProgressCallback(progressCallback);

        apiPage.loadApiData();

        if (!apiPage.isApiDataLoaded()) {
            throw new RuntimeException("API данные не загрузились корректно");
        }

        String json = apiPage.getJsonData();

        if (!jsonParser.isValidJson(json)) {
            throw new RuntimeException("Получен невалидный JSON из API");
        }

        Map<String, ProblemInfo> problems = jsonParser.parseProblemsFromJson(json);
        logProgress("Загружено всего задач: " + problems.size());

        return problems;
    }

    /**
     * Сопоставляет решенные задачи с их номерами
     */
    private Set<Integer> matchSolvedProblems(Set<String> solvedTitles, Map<String, ProblemInfo> allProblems) {
        logProgress("Сопоставляем названия с номерами задач...");

        Set<Integer> solvedNumbers = problemMatcher.matchProblemsToNumbers(solvedTitles, allProblems);

        logProgress("Сопоставлено задач: " + solvedNumbers.size() + " из " + solvedTitles.size());
        return solvedNumbers;
    }

    /**
     * Получает статистику кэша
     */
    public CacheService.CacheStats getCacheStats() {
        return cacheService.getStats();
    }

    /**
     * Очищает кэш
     */
    public void clearCache() {
        cacheService.clearCache();
    }

    /**
     * Проверяет валидность username
     */
    public boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                username.matches("[a-zA-Z0-9_-]+") &&
                username.length() >= 3 &&
                username.length() <= 20;
    }
}