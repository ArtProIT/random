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
 * ������� ������ ��� ��������� ������ � LeetCode
 * ������������ ������ ���� ����������� �������
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

        // �������� callback ���� �����������
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
     * �������� ������ �������� ����� ��� ������������
     */
    public Set<Integer> fetchSolvedProblems(String username, boolean headless) throws LeetCodeScrapingException {
        logProgress("�������� ��������� �������� ����� ��� ������������: " + username);
        logProgress("����� ��������: " + (headless ? "headless (�������)" : "������� (�������)"));

        try (BrowserManager browserManager = new BrowserManager(headless)) {
            browserManager.setProgressCallback(progressCallback);

            // �������� �������� �������� �����
            Set<String> solvedTitles = getSolvedProblemsFromProfile(browserManager, username);

            if (solvedTitles.isEmpty()) {
                logProgress("�������� ������ �� �������");
                return Set.of();
            }

            // �������� ��� ������ � ��������
            Map<String, ProblemInfo> allProblems = getAllProblemsInfo(browserManager);

            // ������������ �������� � ��������
            Set<Integer> solvedNumbers = matchSolvedProblems(solvedTitles, allProblems);

            logProgress("������� �������� �������!");
            return solvedNumbers;

        } catch (Exception e) {
            String errorMsg = "������ ��� ��������� �������� �����: " + e.getMessage();
            logProgress(errorMsg);
            throw new LeetCodeScrapingException(errorMsg, e);
        }
    }

    /**
     * �������� ���������� � ���� ������� � ��������� ����
     */
    public Map<String, ProblemInfo> getAllProblemsInfo(boolean headless) throws ApiDataException {
        // ��������� ���
        Map<String, ProblemInfo> cachedProblems = cacheService.getCachedProblems();
        if (cachedProblems != null) {
            return cachedProblems;
        }

        logProgress("����� ��������: " + (headless ? "headless (�������)" : "������� (�������)"));

        try (BrowserManager browserManager = new BrowserManager(headless)) {
            browserManager.setProgressCallback(progressCallback);
            Map<String, ProblemInfo> problems = getAllProblemsInfo(browserManager);

            // ��������� � ���
            cacheService.cacheProblems(problems);

            return problems;

        } catch (Exception e) {
            throw new ApiDataException("������ ��� ��������� ���������� � �������", e);
        }
    }

    /**
     * �������� �������� ������ �� ������� � ���������� ���������
     */
    private Set<String> getSolvedProblemsFromProfile(BrowserManager browserManager, String username)
            throws LeetCodeScrapingException {

        return retryManager.executeWithRetry(
                () -> {
                    LeetCodeProfilePage profilePage = new LeetCodeProfilePage(browserManager.createPage());
                    profilePage.setProgressCallback(progressCallback);

                    profilePage.openProfile(username);
                    profilePage.openRecentAC(); // ����� �� ���������, �� ��� ���������

                    return profilePage.extractSolvedProblems();
                },
                "��������� �������� ����� �� �������",
                result -> result != null && !result.isEmpty()
        );
    }

    /**
     * �������� ��� ������ �� API
     */
    private Map<String, ProblemInfo> getAllProblemsInfo(BrowserManager browserManager) throws Exception {
        logProgress("�������� ������ ������ ����� ����� API...");

        LeetCodeApiPage apiPage = new LeetCodeApiPage(browserManager.createPage());
        apiPage.setProgressCallback(progressCallback);

        apiPage.loadApiData();

        if (!apiPage.isApiDataLoaded()) {
            throw new RuntimeException("API ������ �� ����������� ���������");
        }

        String json = apiPage.getJsonData();

        if (!jsonParser.isValidJson(json)) {
            throw new RuntimeException("������� ���������� JSON �� API");
        }

        Map<String, ProblemInfo> problems = jsonParser.parseProblemsFromJson(json);
        logProgress("��������� ����� �����: " + problems.size());

        return problems;
    }

    /**
     * ������������ �������� ������ � �� ��������
     */
    private Set<Integer> matchSolvedProblems(Set<String> solvedTitles, Map<String, ProblemInfo> allProblems) {
        logProgress("������������ �������� � �������� �����...");

        Set<Integer> solvedNumbers = problemMatcher.matchProblemsToNumbers(solvedTitles, allProblems);

        logProgress("������������ �����: " + solvedNumbers.size() + " �� " + solvedTitles.size());
        return solvedNumbers;
    }

    /**
     * �������� ���������� ����
     */
    public CacheService.CacheStats getCacheStats() {
        return cacheService.getStats();
    }

    /**
     * ������� ���
     */
    public void clearCache() {
        cacheService.clearCache();
    }

    /**
     * ��������� ���������� username
     */
    public boolean isValidUsername(String username) {
        return username != null &&
                !username.trim().isEmpty() &&
                username.matches("[a-zA-Z0-9_-]+") &&
                username.length() >= 3 &&
                username.length() <= 20;
    }
}