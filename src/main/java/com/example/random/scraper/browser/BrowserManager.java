package com.example.random.scraper.browser;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Управляет жизненным циклом браузера и созданием страниц
 */
@Slf4j
public class BrowserManager implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final Random random;
    private Consumer<String> progressCallback;

    public BrowserManager() {
        this.random = new Random();
        this.playwright = Playwright.create();
        this.browser = createBrowser();
        log.info("Browser manager initialized");
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        log.info(message);
    }

    private Browser createBrowser() {
        logProgress("Запускаем браузер Chromium...");

        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(300));
    }

    /**
     * Создает новую страницу с настроенными заголовками
     */
    public Page createPage() {
        Page page = browser.newPage();

        // Устанавливаем случайный User-Agent
        String userAgent = ScrapingConfig.USER_AGENTS[random.nextInt(ScrapingConfig.USER_AGENTS.length)];
        page.setExtraHTTPHeaders(Map.of("User-Agent", userAgent));

        log.debug("Created new page with User-Agent: {}", userAgent);
        return page;
    }

    @Override
    public void close() {
        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
                logProgress("Браузер закрыт");
            }

            if (playwright != null) {
                playwright.close();
                logProgress("Playwright закрыт");
            }
        } catch (Exception e) {
            log.error("Error closing browser manager", e);
        }
    }
}