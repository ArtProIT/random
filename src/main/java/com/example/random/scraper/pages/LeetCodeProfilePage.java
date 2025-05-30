package com.example.random.scraper.pages;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Page Object для страницы профиля пользователя LeetCode
 */
@Slf4j
public class LeetCodeProfilePage extends BasePage {

    public LeetCodeProfilePage(Page page) {
        super(page);
    }

    /**
     * Открывает профиль пользователя
     */
    public void openProfile(String username) {
        String profileUrl = String.format(ScrapingConfig.LEETCODE_PROFILE_URL_TEMPLATE, username);
        navigateAndWait(profileUrl);
        logProgress("Профиль пользователя " + username + " загружен");
    }

    /**
     * Пытается открыть раздел Recent AC
     */
    public boolean openRecentAC() {
        logProgress("Ищем раздел с решенными задачами...");

        boolean success = tryClickAnySelector(
                ScrapingConfig.RECENT_AC_SELECTORS,
                "кнопку Recent AC"
        );

        if (success) {
            try {
                Thread.sleep(ScrapingConfig.SOLVED_PROBLEMS_WAIT);
                logProgress("Раздел Recent AC открыт");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Recent AC", e);
            }
        }

        return success;
    }

    /**
     * Извлекает названия решенных задач
     */
    public Set<String> extractSolvedProblems() {
        Set<String> solvedTitles = new HashSet<>();

        logProgress("Извлекаем названия решенных задач...");

        for (String selector : ScrapingConfig.SOLVED_PROBLEM_SELECTORS) {
            try {
                List<ElementHandle> elements = page.querySelectorAll(selector);
                logProgress("Найдено элементов с селектором '" + selector + "': " + elements.size());

                for (ElementHandle element : elements) {
                    String title = extractTitleFromElement(element);
                    if (isValidTitle(title)) {
                        solvedTitles.add(title);
                    }
                }

                if (!solvedTitles.isEmpty()) {
                    logProgress("Успешно извлечено " + solvedTitles.size() + " названий задач");
                    break;
                }
            } catch (Exception e) {
                logProgress("Ошибка с селектором " + selector + ": " + e.getMessage());
            }
        }

        return solvedTitles;
    }

    /**
     * Извлекает название задачи из DOM элемента
     */
    private String extractTitleFromElement(ElementHandle element) {
        try {
            // Сначала пробуем атрибут data-title
            String dataTitle = element.getAttribute("data-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }

            // Затем пробуем текстовое содержимое
            String text = element.textContent();
            if (text != null && !text.trim().isEmpty()) {
                String[] lines = text.split("\\n");
                String firstLine = lines[0].trim();
                // Убираем номер задачи в начале строки
                return firstLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            }
        } catch (Exception e) {
            log.debug("Error extracting title from element: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Проверяет, валидно ли название задачи
     */
    private boolean isValidTitle(String title) {
        return title != null &&
                !title.isEmpty() &&
                title.length() > 3 &&
                !title.contains("days ago") &&
                !title.matches("\\d+");
    }

}