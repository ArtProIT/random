package com.example.random.service;

import com.example.random.scraper.LeetCodeScraper;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Сервис для работы с данными LeetCode
 */
public class LeetCodeService {
    private final LeetCodeScraper scraper;

    public LeetCodeService() {
        this.scraper = new LeetCodeScraper();
    }


    public Set<Integer> getSolvedProblems(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }

        return scraper.fetchSolvedProblems(username.trim());
    }


    public Set<Integer> getSolvedProblems(String username, Consumer<String> progressCallback) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }

        scraper.setProgressCallback(progressCallback);
        return scraper.fetchSolvedProblems(username.trim());
    }

    /**
     * Проверяет валидность username
     */
    public boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }
}