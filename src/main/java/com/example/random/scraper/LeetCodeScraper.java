package com.example.random.scraper;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

/**
 * ����� ��� ��������� ������ � LeetCode ����� ���-��������
 */
public class LeetCodeScraper {
    private static final int DEFAULT_WAIT_TIME = 3000;
    private static final int JSON_WAIT_TIME = 2000;
    private static final int SOLVED_PROBLEMS_WAIT = 5000;

    private Consumer<String> progressCallback;

    /**
     * ������������� callback ��� ����������� ���������
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
     * �������� ������ �������� ����� ��� ���������� ������������
     */
    public Set<Integer> fetchSolvedProblems(String username) {
        Set<Integer> solvedProblems = new HashSet<>();

        try (Playwright playwright = Playwright.create()) {
            logProgress("��������� ������� Chromium...");
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(300));

            Page page = browser.newPage();

            logProgress("��������� �� ������� ������������: " + username);

            // �������� �������� ������
            Set<String> solvedTitles = getSolvedProblemsFromProfile(page, username);
            if (solvedTitles.isEmpty()) {
                logProgress("�������� ������ �� �������");
                browser.close();
                return solvedProblems;
            }

            logProgress("������� �������� �����: " + solvedTitles.size());

            // �������� ��� ������ � ��������
            logProgress("�������� ������ ������ ����� ����� API...");
            Map<String, Integer> allProblems = getAllProblemsFromAPI(page);
            logProgress("��������� ����� �����: " + allProblems.size());

            // ������������ �������� ������ � ��������
            logProgress("������������ �������� � �������� �����...");
            solvedProblems = matchSolvedProblemsWithNumbers(solvedTitles, allProblems);

            browser.close();
            logProgress("������� �������� �������!");

        } catch (Exception e) {
            logProgress("������ ��� ��������� ������: " + e.getMessage());
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
            logProgress("������� ������������ ��������");

            // ������� ����� � �������� Recent AC
            logProgress("���� ������ � ��������� ��������...");
            if (clickRecentAC(page)) {
                logProgress("������ � ������ ������ Recent AC");
                Thread.sleep(SOLVED_PROBLEMS_WAIT);
            } else {
                logProgress("������ Recent AC �� ������, ���� �������� ������ �� ��������");
            }

            // �������� ������ � �������� �������
            logProgress("��������� �������� �������� �����...");
            solvedTitles = extractSolvedProblems(page);

        } catch (Exception e) {
            logProgress("������ ��� ��������� �������� �����: " + e.getMessage());
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
                    logProgress("������� ������ Recent AC: " + selector);
                    page.click(selector);
                    return true;
                }
            } catch (Exception e) {
                // ���������� ����� ������� �����������
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

    private String extractTitleFromElement(ElementHandle element) {
        try {
            // ������� �������� title �� data-title ��������
            String dataTitle = element.getAttribute("data-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }

            // ����� ����� ����� ��������
            String text = element.textContent();
            if (text != null && !text.trim().isEmpty()) {
                String[] lines = text.split("\\n");
                String firstLine = lines[0].trim();
                return firstLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            }
        } catch (Exception e) {
            System.out.println("������ ���������� ���������: " + e.getMessage());
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
            logProgress("API ������ ��������");

            String json = (String) page.evaluate("() => document.body.innerText");
            allProblems = parseProblemsFromJSON(json);

        } catch (Exception e) {
            logProgress("������ ��� ��������� ����� �� JSON API: " + e.getMessage());
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

            logProgress("���������� ����� �� API: " + allProblems.size());

        } catch (Exception e) {
            logProgress("������ �������� JSON: " + e.getMessage());
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

        logProgress("������������ �����: " + matchedCount + " �� " + solvedTitles.size());
        logProgress("����� ������� ������� �������� �����: " + solvedProblems.size());

        return solvedProblems;
    }
}