package com.example.random.scraper.pages;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

/**
 * Page Object ��� API �������� LeetCode � ������� � �������
 */
@Slf4j
public class LeetCodeApiPage extends BasePage {

    public LeetCodeApiPage(Page page) {
        super(page);
    }

    /**
     * ��������� API ������
     */
    public void loadApiData() {
        navigateAndWait(ScrapingConfig.LEETCODE_API_URL, ScrapingConfig.JSON_WAIT_TIME);
        logProgress("API ������ ��������");
    }

    /**
     * �������� JSON ������ �� ��������
     */
    public String getJsonData() {
        String json = getPageText();

        if (json == null || json.trim().isEmpty()) {
            throw new RuntimeException("API returned empty response");
        }

        // ���������, ��� ��� �������� JSON
        if (!json.trim().startsWith("{")) {
            throw new RuntimeException("API response is not valid JSON");
        }

        log.debug("Retrieved JSON data, length: {}", json.length());
        return json;
    }

    /**
     * ���������, ��� API �������� ����������� ���������
     */
    public boolean isApiDataLoaded() {
        try {
            String pageText = getPageText();
            return pageText != null &&
                    pageText.contains("stat_status_pairs") &&
                    pageText.trim().startsWith("{");
        } catch (Exception e) {
            log.error("Failed to check if API data is loaded", e);
            return false;
        }
    }

}