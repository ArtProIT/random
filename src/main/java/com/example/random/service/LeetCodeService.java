package com.example.random.service;

import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemInfo;
import com.example.random.scraper.services.LeetCodeScrapingService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Обновленный сервис для работы с LeetCode
 * Использует новую архитектуру на основе Page Object Model
 */
@Slf4j
public class LeetCodeService {
    private final LeetCodeScrapingService scrapingService;

    public LeetCodeService() {
        this.scrapingService = new LeetCodeScrapingService();
    }

    /**
     * Получает решенные задачи пользователя
     */
    public Set<Integer> getSolvedProblems(String username, Consumer<String> progressCallback)
            throws LeetCodeScrapingException, ValidationException {

        if (!isValidUsername(username)) {
            throw new ValidationException("Некорректный username: " + username);
        }

        log.info("Получаем решенные задачи для пользователя: {}", username);

        scrapingService.setProgressCallback(progressCallback);

        try {
            Set<Integer> solvedProblems = scrapingService.fetchSolvedProblems(username);
            log.info("Найдено решенных задач: {}", solvedProblems.size());
            return solvedProblems;

        } catch (LeetCodeScrapingException e) {
            log.error("Ошибка при получении решенных задач для пользователя: {}", username, e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении решенных задач", e);
            throw new LeetCodeScrapingException("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Получает информацию о всех задачах
     */
    public Map<String, ProblemInfo> getAllProblemsInfo(Consumer<String> progressCallback)
            throws ApiDataException {

        log.info("Получаем информацию о всех задачах");

        scrapingService.setProgressCallback(progressCallback);

        try {
            Map<String, ProblemInfo> problems = scrapingService.getAllProblemsInfo();
            log.info("Загружено задач: {}", problems.size());
            return problems;

        } catch (ApiDataException e) {
            log.error("Ошибка при получении информации о задачах", e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении информации о задачах", e);
            throw new ApiDataException("Неожиданная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяет валидность username
     */
    public boolean isValidUsername(String username) {
        return scrapingService.isValidUsername(username);
    }

    /**
     * Получает статистику кэша
     */
    public String getCacheInfo() {
        var stats = scrapingService.getCacheStats();
        return stats.toString();
    }

    /**
     * Очищает кэш
     */
    public void clearCache() {
        log.info("Очищаем кэш LeetCode данных");
        scrapingService.clearCache();
    }
}