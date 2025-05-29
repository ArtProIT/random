package com.example.random.service;

import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemInfo;
import com.example.random.scraper.LeetCodeScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Улучшенный сервис для работы с данными LeetCode
 */
public class LeetCodeService {
    private static final Logger logger = LoggerFactory.getLogger(LeetCodeService.class);

    private final LeetCodeScraper scraper;

    public LeetCodeService() {
        this.scraper = new LeetCodeScraper();
    }

    /**
     * Получает номера решенных задач для указанного пользователя
     */
    public Set<Integer> getSolvedProblems(String username) throws LeetCodeScrapingException, ValidationException {
        validateUsername(username);
        logger.info("Fetching solved problems for user: {}", username);

        try {
            return scraper.fetchSolvedProblems(username.trim());
        } catch (LeetCodeScrapingException e) {
            logger.error("Failed to fetch solved problems for user: {}", username, e);
            throw e;
        }
    }

    /**
     * Получает номера решенных задач с callback для отображения прогресса
     */
    public Set<Integer> getSolvedProblems(String username, Consumer<String> progressCallback)
            throws LeetCodeScrapingException, ValidationException {
        validateUsername(username);

        scraper.setProgressCallback(progressCallback);
        return getSolvedProblems(username);
    }

    /**
     * Получает полную информацию о всех задачах
     */
    public Map<String, ProblemInfo> getAllProblemsInfo() throws ApiDataException {
        logger.info("Fetching all problems info from API");

        try {
            return scraper.getAllProblemsInfo();
        } catch (ApiDataException e) {
            logger.error("Failed to fetch problems info from API", e);
            throw e;
        }
    }

    /**
     * Получает полную информацию о всех задачах с callback для отображения прогресса
     */
    public Map<String, ProblemInfo> getAllProblemsInfo(Consumer<String> progressCallback) throws ApiDataException {
        scraper.setProgressCallback(progressCallback);
        return getAllProblemsInfo();
    }

    /**
     * Проверяет валидность username
     */
    public boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && username.trim().length() >= 2;
    }

    private void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username не может быть пустым");
        }

        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 2) {
            throw new ValidationException("Username должен содержать минимум 2 символа");
        }

        if (!trimmedUsername.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ValidationException("Username может содержать только буквы, цифры, дефисы и подчеркивания");
        }
    }
}