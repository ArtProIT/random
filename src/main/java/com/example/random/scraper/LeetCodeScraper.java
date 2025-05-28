package com.example.random.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

/**
 * Класс для получения данных с LeetCode через веб-скрапинг
 */
public class LeetCodeScraper {
    private static final int DEFAULT_WAIT_TIME = 3000;
    private static final int JSON_WAIT_TIME = 2000;
    private static final int SOLVED_PROBLEMS_WAIT = 5000;

    private Consumer<String> progressCallback;

    /**
     * Устанавливает callback для отображения прогресса
     */
    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        System.out.println(message);
    }

    /**
     * Получает номера решенных задач для указанного пользователя
     */
    public Set<Integer> fetchSolvedProblems(String username) {
        Set<Integer> solvedProblems = new HashSet<>();

        try (Playwright playwright = Playwright.create()) {
            logProgress("Запускаем браузер Chromium...");
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(300));

            Page page = browser.newPage();

            logProgress("Переходим на профиль пользователя: " + username);

            // Получаем решенные задачи
            Set<String> solvedTitles = getSolvedProblemsFromProfile(page, username);
            if (solvedTitles.isEmpty()) {
                logProgress("Решенные задачи не найдены");
                browser.close();
                return solvedProblems;
            }

            logProgress("Найдено решенных задач: " + solvedTitles.size());

            // Получаем все задачи с номерами
            logProgress("Получаем полный список задач через API...");
            Map<String, Integer> allProblems = getAllProblemsFromAPI(page);
            logProgress("Загружено всего задач: " + allProblems.size());

            // Сопоставляем решенные задачи с номерами
            logProgress("Сопоставляем названия с номерами задач...");
            solvedProblems = matchSolvedProblemsWithNumbers(solvedTitles, allProblems);

            browser.close();
            logProgress("Процесс завершен успешно!");

        } catch (Exception e) {
            logProgress("Ошибка при получении данных: " + e.getMessage());
            e.printStackTrace();
        }

        return solvedProblems;
    }

    private Set<String> getSolvedProblemsFromProfile(Page page, String username) {
        Set<String> solvedTitles = new HashSet<>();

        try {
            String profileUrl = "https://leetcode.com/u/" + username + "/";
            page.navigate(profileUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(DEFAULT_WAIT_TIME);
            logProgress("Профиль пользователя загружен");

            // Попытка найти и кликнуть Recent AC
            logProgress("Ищем раздел с решенными задачами...");
            if (clickRecentAC(page)) {
                logProgress("Найден и открыт раздел Recent AC");
                Thread.sleep(SOLVED_PROBLEMS_WAIT);
            } else {
                logProgress("Раздел Recent AC не найден, ищем решенные задачи на странице");
            }

            // Собираем данные о решенных задачах
            logProgress("Извлекаем названия решенных задач...");
            solvedTitles = extractSolvedProblems(page);

        } catch (Exception e) {
            logProgress("Ошибка при получении решенных задач: " + e.getMessage());
            e.printStackTrace();
        }

        return solvedTitles;
    }

    private boolean clickRecentAC(Page page) {
        String[] recentAcSelectors = {
                "text=Recent AC",
                "[data-cy='recent-ac']",
                "button:has-text('Recent AC')",
                "a:has-text('Recent AC')",
                ".recent-ac",
                "[aria-label*='Recent']",
                "span:has-text('Recent AC')"
        };

        for (String selector : recentAcSelectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    logProgress("Найдена кнопка Recent AC: " + selector);
                    page.click(selector);
                    return true;
                }
            } catch (Exception e) {
                // Продолжаем поиск другими селекторами
            }
        }

        return false;
    }

    private Set<String> extractSolvedProblems(Page page) {
        Set<String> solvedTitles = new HashSet<>();

        String[] solvedSelectors = {
                "[data-title]",
                "span.text-label-1",
                "a[href*='/submissions/detail/']"
        };

        for (String selector : solvedSelectors) {
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

    private String extractTitleFromElement(ElementHandle element) {
        try {
            // Попытка получить title из data-title атрибута
            String dataTitle = element.getAttribute("data-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }

            // Иначе берем текст элемента
            String text = element.textContent();
            if (text != null && !text.trim().isEmpty()) {
                String[] lines = text.split("\\n");
                String firstLine = lines[0].trim();
                return firstLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            }
        } catch (Exception e) {
            System.out.println("Ошибка извлечения заголовка: " + e.getMessage());
        }

        return null;
    }

    private boolean isValidTitle(String title) {
        return title != null && !title.isEmpty() && title.length() > 3 &&
                !title.contains("days ago") && !title.matches("\\d+");
    }

    private Map<String, Integer> getAllProblemsFromAPI(Page page) {
        Map<String, Integer> allProblems = new HashMap<>();

        try {
            page.navigate("https://leetcode.com/api/problems/all/");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            Thread.sleep(JSON_WAIT_TIME);
            logProgress("API данные получены");

            String json = (String) page.evaluate("() => document.body.innerText");
            allProblems = parseProblemsFromJSON(json);

        } catch (Exception e) {
            logProgress("Ошибка при получении задач из JSON API: " + e.getMessage());
            e.printStackTrace();
        }

        return allProblems;
    }

    private Map<String, Integer> parseProblemsFromJSON(String json) {
        Map<String, Integer> allProblems = new HashMap<>();

        try {
            JSONObject root = new JSONObject(json);
            JSONArray problems = root.getJSONArray("stat_status_pairs");

            for (int i = 0; i < problems.length(); i++) {
                JSONObject stat = problems.getJSONObject(i).getJSONObject("stat");
                int number = stat.getInt("frontend_question_id");
                String title = stat.getString("question__title").trim();
                allProblems.put(title, number);
            }

            logProgress("Обработано задач из API: " + allProblems.size());

        } catch (Exception e) {
            logProgress("Ошибка парсинга JSON: " + e.getMessage());
        }

        return allProblems;
    }

    private Set<Integer> matchSolvedProblemsWithNumbers(Set<String> solvedTitles, Map<String, Integer> allProblems) {
        Set<Integer> solvedProblems = new HashSet<>();
        ProblemMatcher matcher = new ProblemMatcher();

        int matchedCount = 0;
        for (String solvedTitle : solvedTitles) {
            Integer problemNumber = matcher.findProblemNumber(solvedTitle, allProblems);
            if (problemNumber != null) {
                solvedProblems.add(problemNumber);
                matchedCount++;
            }
        }

        logProgress("Сопоставлено задач: " + matchedCount + " из " + solvedTitles.size());
        logProgress("Итого найдено номеров решенных задач: " + solvedProblems.size());

        return solvedProblems;
    }
}