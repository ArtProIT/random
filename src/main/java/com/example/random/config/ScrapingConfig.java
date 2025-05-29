package com.example.random.config;

/**
 * Конфигурация для веб-скрапинга
 */
public class ScrapingConfig {
    public static final int DEFAULT_WAIT_TIME = 3000;
    public static final int JSON_WAIT_TIME = 2000;
    public static final int SOLVED_PROBLEMS_WAIT = 5000;
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY = 1000;
    public static final String LEETCODE_API_URL = "https://leetcode.com/api/problems/all/";
    public static final String LEETCODE_PROFILE_URL_TEMPLATE = "https://leetcode.com/u/%s/";

    public static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    };

    // Селекторы для поиска решенных задач
    public static final String[] RECENT_AC_SELECTORS = {
            "text=Recent AC",
            "[data-cy='recent-ac']",
            "button:has-text('Recent AC')",
            "a:has-text('Recent AC')",
            ".recent-ac",
            "[aria-label*='Recent']",
            "span:has-text('Recent AC')"
    };

    public static final String[] SOLVED_PROBLEM_SELECTORS = {
            "[data-title]",
            "span.text-label-1",
            "a[href*='/submissions/detail/']"
    };
}