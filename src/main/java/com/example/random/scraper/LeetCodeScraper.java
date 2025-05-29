package com.example.random.scraper;

import com.example.random.config.ScrapingConfig;
import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemDifficulty;
import com.example.random.model.ProblemInfo;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * ���������� ����� ��� ��������� ������ � LeetCode ����� ���-��������
 */
public class LeetCodeScraper {
    private static final Logger logger = LoggerFactory.getLogger(LeetCodeScraper.class);

    private static final Map<String, Map<String, ProblemInfo>> problemsCache = new ConcurrentHashMap<>();
    private static long cacheTimestamp = 0;
    private static final long CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000;

    private Consumer<String> progressCallback;
    private final Random random = new Random();

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        logger.info(message);
    }

    /**
     * �������� ������ �������� ����� ��� ���������� ������������
     */
    public Set<Integer> fetchSolvedProblems(String username) throws LeetCodeScrapingException {
        Set<Integer> solvedProblems = new HashSet<>();

        try (Playwright playwright = Playwright.create()) {
            logProgress("��������� ������� Chromium...");
            Browser browser = createBrowser(playwright);
            Page page = createPage(browser);

            logProgress("��������� �� ������� ������������: " + username);

            // �������� �������� ������ � retry �������
            Set<String> solvedTitles = getSolvedProblemsWithRetry(page, username);
            if (solvedTitles.isEmpty()) {
                logProgress("�������� ������ �� �������");
                browser.close();
                return solvedProblems;
            }

            logProgress("������� �������� �����: " + solvedTitles.size());

            // �������� ��� ������ � ��������
            logProgress("�������� ������ ������ ����� ����� API...");
            Map<String, ProblemInfo> allProblems = getAllProblemsFromAPI(page);
            logProgress("��������� ����� �����: " + allProblems.size());

            // ������������ �������� ������ � ��������
            logProgress("������������ �������� � �������� �����...");
            solvedProblems = matchSolvedProblemsWithNumbers(solvedTitles, allProblems);

            browser.close();
            logProgress("������� �������� �������!");

        } catch (Exception e) {
            String errorMsg = "������ ��� ��������� ������: " + e.getMessage();
            logProgress(errorMsg);
            logger.error(errorMsg, e);
            throw new LeetCodeScrapingException(errorMsg, e);
        }

        return solvedProblems;
    }

    /**
     * �������� ��� ������ �� API � ����������� � ���������
     */
    public Map<String, ProblemInfo> getAllProblemsInfo() throws ApiDataException {
        // ��������� ���
        if (isCacheValid()) {
            logProgress("���������� ������������ ������ API");
            return new HashMap<>(problemsCache.get("problems"));
        }

        try (Playwright playwright = Playwright.create()) {
            Browser browser = createBrowser(playwright);
            Page page = createPage(browser);

            Map<String, ProblemInfo> problems = getAllProblemsFromAPI(page);

            // ��������� ���
            problemsCache.put("problems", problems);
            cacheTimestamp = System.currentTimeMillis();

            browser.close();
            return problems;

        } catch (Exception e) {
            throw new ApiDataException("Failed to fetch problems info", e);
        }
    }

    private Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(300));
    }

    private Page createPage(Browser browser) {
        Page page = browser.newPage();

        // ������������� ��������� User-Agent
        String userAgent = ScrapingConfig.USER_AGENTS[random.nextInt(ScrapingConfig.USER_AGENTS.length)];
        page.setExtraHTTPHeaders(Map.of("User-Agent", userAgent));

        return page;
    }

    private Set<String> getSolvedProblemsWithRetry(Page page, String username) throws LeetCodeScrapingException {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
            try {
                attempts++;
                logProgress("������� " + attempts + " ��������� �������� �����...");

                Set<String> result = getSolvedProblemsFromProfile(page, username);
                if (!result.isEmpty()) {
                    return result;
                }

                if (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
                    logProgress("�������� ������ �� �������, ��������� ����� " + ScrapingConfig.RETRY_DELAY + "��...");
                    Thread.sleep(ScrapingConfig.RETRY_DELAY);
                }

            } catch (Exception e) {
                lastException = e;
                logProgress("������ � ������� " + attempts + ": " + e.getMessage());

                if (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(ScrapingConfig.RETRY_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new LeetCodeScrapingException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new LeetCodeScrapingException("Failed to get solved problems after " +
                ScrapingConfig.MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }

    private Set<String> getSolvedProblemsFromProfile(Page page, String username) throws Exception {
        Set<String> solvedTitles = new HashSet<>();

        String profileUrl = String.format(ScrapingConfig.LEETCODE_PROFILE_URL_TEMPLATE, username);
        page.navigate(profileUrl);


        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.waitForFunction("() => document.readyState === 'complete'");
        Thread.sleep(ScrapingConfig.DEFAULT_WAIT_TIME);

        logProgress("������� ������������ ��������");

        // ������� ����� � �������� Recent AC
        logProgress("���� ������ � ��������� ��������...");
        if (clickRecentAC(page)) {
            logProgress("������ � ������ ������ Recent AC");
            Thread.sleep(ScrapingConfig.SOLVED_PROBLEMS_WAIT);
        } else {
            logProgress("������ Recent AC �� ������, ���� �������� ������ �� ��������");
        }

        // �������� ������ � �������� �������
        logProgress("��������� �������� �������� �����...");
        solvedTitles = extractSolvedProblems(page);

        return solvedTitles;
    }

    private boolean clickRecentAC(Page page) {
        for (String selector : ScrapingConfig.RECENT_AC_SELECTORS) {
            try {
                if (page.locator(selector).count() > 0) {
                    logProgress("������� ������ Recent AC: " + selector);
                    page.click(selector);
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Selector {} failed: {}", selector, e.getMessage());
            }
        }
        return false;
    }

    private Set<String> extractSolvedProblems(Page page) {
        Set<String> solvedTitles = new HashSet<>();

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

    private String extractTitleFromElement(ElementHandle element) {
        try {
            String dataTitle = element.getAttribute("data-title");
            if (dataTitle != null && !dataTitle.trim().isEmpty()) {
                return dataTitle.trim();
            }

            String text = element.textContent();
            if (text != null && !text.trim().isEmpty()) {
                String[] lines = text.split("\\n");
                String firstLine = lines[0].trim();
                return firstLine.replaceFirst("^\\d+\\.\\s*", "").trim();
            }
        } catch (Exception e) {
            logger.debug("Error extracting title: {}", e.getMessage());
        }

        return null;
    }

    private boolean isValidTitle(String title) {
        return title != null && !title.isEmpty() && title.length() > 3 &&
                !title.contains("days ago") && !title.matches("\\d+");
    }

    private Map<String, ProblemInfo> getAllProblemsFromAPI(Page page) throws Exception {
        Map<String, ProblemInfo> allProblems = new HashMap<>();

        page.navigate(ScrapingConfig.LEETCODE_API_URL);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(ScrapingConfig.JSON_WAIT_TIME);
        logProgress("API ������ ��������");

        String json = (String) page.evaluate("() => document.body.innerText");
        allProblems = parseProblemsFromJSON(json);

        return allProblems;
    }

    private Map<String, ProblemInfo> parseProblemsFromJSON(String json) {
        Map<String, ProblemInfo> allProblems = new HashMap<>();

        try {
            JSONObject root = new JSONObject(json);
            JSONArray problems = root.getJSONArray("stat_status_pairs");

            for (int i = 0; i < problems.length(); i++) {
                JSONObject stat = problems.getJSONObject(i).getJSONObject("stat");
                JSONObject difficulty = problems.getJSONObject(i).getJSONObject("difficulty");

                int number = stat.getInt("frontend_question_id");
                String title = stat.getString("question__title").trim();
                int level = difficulty.getInt("level");

                ProblemDifficulty problemDifficulty = ProblemDifficulty.fromLevel(level);
                ProblemInfo problemInfo = new ProblemInfo(number, title, problemDifficulty);

                allProblems.put(title, problemInfo);
            }

            logProgress("���������� ����� �� API: " + allProblems.size());

        } catch (Exception e) {
            logProgress("������ �������� JSON: " + e.getMessage());
            logger.error("JSON parsing error", e);
        }

        return allProblems;
    }

    private Set<Integer> matchSolvedProblemsWithNumbers(Set<String> solvedTitles, Map<String, ProblemInfo> allProblems) {
        Set<Integer> solvedProblems = new HashSet<>();
        ProblemMatcher matcher = new ProblemMatcher();

        int matchedCount = 0;
        for (String solvedTitle : solvedTitles) {
            ProblemInfo problemInfo = matcher.findProblemInfo(solvedTitle, allProblems);
            if (problemInfo != null) {
                solvedProblems.add(problemInfo.getNumber());
                matchedCount++;
            }
        }

        logProgress("������������ �����: " + matchedCount + " �� " + solvedTitles.size());
        logProgress("����� ������� ������� �������� �����: " + solvedProblems.size());

        return solvedProblems;
    }

    private boolean isCacheValid() {
        return problemsCache.containsKey("problems") &&
                (System.currentTimeMillis() - cacheTimestamp) < CACHE_EXPIRY_TIME;
    }
}