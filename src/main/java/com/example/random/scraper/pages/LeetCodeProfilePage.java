package com.example.random.scraper.pages;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Page Object ��� �������� ������� ������������ LeetCode
 */
@Slf4j
public class LeetCodeProfilePage extends BasePage {

    public LeetCodeProfilePage(Page page) {
        super(page);
    }

    /**
     * ��������� ������� ������������
     */
    public void openProfile(String username) {
        String profileUrl = String.format(ScrapingConfig.LEETCODE_PROFILE_URL_TEMPLATE, username);
        navigateAndWait(profileUrl);
        logProgress("������� ������������ " + username + " ��������");
    }

    /**
     * �������� ������� ������ Recent AC
     */
    public boolean openRecentAC() {
        logProgress("���� ������ � ��������� ��������...");

        boolean success = tryClickAnySelector(
                ScrapingConfig.RECENT_AC_SELECTORS,
                "������ Recent AC"
        );

        if (success) {
            try {
                Thread.sleep(ScrapingConfig.SOLVED_PROBLEMS_WAIT);
                logProgress("������ Recent AC ������");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Recent AC", e);
            }
        }

        return success;
    }

    /**
     * ��������� �������� �������� �����
     */
    public Set<String> extractSolvedProblems() {
        Set<String> solvedTitles = new HashSet<>();

        logProgress("��������� �������� �������� �����...");

        for (String selector : ScrapingConfig.SOLVED_PROBLEM_SELECTORS) {
            try {
                List<ElementHandle> elements = page.querySelectorAll(selector);
                logProgress("������� ��������� � ���������� '" + selector + "': " + elements.size());

                for (ElementHandle element : elements) {
                    String title = extractTitleFromElement(element);
                    if (isValidTitle(title)) {
                        solvedTitles.add(title);
                    }
                }

                if (!solvedTitles.isEmpty()) {
                    logProgress("������� ��������� " + solvedTitles.size() + " �������� �����");
                    break;
                }
            } catch (Exception e) {
                logProgress("������ � ���������� " + selector + ": " + e.getMessage());
            }
        }

        return solvedTitles;
    }

    /**
     * ��������� �������� ������ �� DOM ��������
     */
    private String extractTitleFromElement(ElementHandle element) {
        try {
            // ������� ������� ������� data-title
            String dataTitle = element.getAttribute("data-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }

            // ����� ������� ��������� ����������
            String text = element.textContent();
            if (text != null && !text.trim().isEmpty()) {
                String[] lines = text.split("\\n");
                String firstLine = lines[0].trim();
                // ������� ����� ������ � ������ ������
                return firstLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            }
        } catch (Exception e) {
            log.debug("Error extracting title from element: {}", e.getMessage());
        }

        return null;
    }

    /**
     * ���������, ������� �� �������� ������
     */
    private boolean isValidTitle(String title) {
        return title != null &&
                !title.isEmpty() &&
                title.length() > 3 &&
                !title.contains("days ago") &&
                !title.matches("\\d+");
    }

}