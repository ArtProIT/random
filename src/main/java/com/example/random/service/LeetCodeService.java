package com.example.random.service;

import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemInfo;
import com.example.random.scraper.services.LeetCodeScrapingService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ����������� ������ ��� ������ � LeetCode
 * ���������� ����� ����������� �� ������ Page Object Model
 */
@Slf4j
public class LeetCodeService {
    private final LeetCodeScrapingService scrapingService;

    public LeetCodeService() {
        this.scrapingService = new LeetCodeScrapingService();
    }

    /**
     * �������� �������� ������ ������������
     */
    public Set<Integer> getSolvedProblems(String username, Consumer<String> progressCallback)
            throws LeetCodeScrapingException, ValidationException {

        if (!isValidUsername(username)) {
            throw new ValidationException("������������ username: " + username);
        }

        log.info("�������� �������� ������ ��� ������������: {}", username);

        scrapingService.setProgressCallback(progressCallback);

        try {
            Set<Integer> solvedProblems = scrapingService.fetchSolvedProblems(username);
            log.info("������� �������� �����: {}", solvedProblems.size());
            return solvedProblems;

        } catch (LeetCodeScrapingException e) {
            log.error("������ ��� ��������� �������� ����� ��� ������������: {}", username, e);
            throw e;
        } catch (Exception e) {
            log.error("����������� ������ ��� ��������� �������� �����", e);
            throw new LeetCodeScrapingException("����������� ������: " + e.getMessage(), e);
        }
    }

    /**
     * �������� ���������� � ���� �������
     */
    public Map<String, ProblemInfo> getAllProblemsInfo(Consumer<String> progressCallback)
            throws ApiDataException {

        log.info("�������� ���������� � ���� �������");

        scrapingService.setProgressCallback(progressCallback);

        try {
            Map<String, ProblemInfo> problems = scrapingService.getAllProblemsInfo();
            log.info("��������� �����: {}", problems.size());
            return problems;

        } catch (ApiDataException e) {
            log.error("������ ��� ��������� ���������� � �������", e);
            throw e;
        } catch (Exception e) {
            log.error("����������� ������ ��� ��������� ���������� � �������", e);
            throw new ApiDataException("����������� ������: " + e.getMessage(), e);
        }
    }

    /**
     * ��������� ���������� username
     */
    public boolean isValidUsername(String username) {
        return scrapingService.isValidUsername(username);
    }

    /**
     * �������� ���������� ����
     */
    public String getCacheInfo() {
        var stats = scrapingService.getCacheStats();
        return stats.toString();
    }

    /**
     * ������� ���
     */
    public void clearCache() {
        log.info("������� ��� LeetCode ������");
        scrapingService.clearCache();
    }
}