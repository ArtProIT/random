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
    private final boolean headless;
    private Consumer<String> progressCallback;

    /**
     * Конструктор с поддержкой headless режима
     */
    public BrowserManager(boolean headless) {
        this.headless = headless;
        this.random = new Random();
        this.playwright = Playwright.create();
        this.browser = createBrowser();
        log.info("Browser manager initialized (headless: {})", headless);
    }

    /**
     * Конструктор по умолчанию (headless = true для обратной совместимости)
     */
    public BrowserManager() {
        this(true);
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
        String modeText = headless ? "скрытом (headless)" : "видимом";
        logProgress("Запускаем браузер Chromium в " + modeText + " режиме...");

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        // Добавляем SlowMo только для не-headless режима для лучшей визуализации
        if (!headless) {
            options.setSlowMo(300);
            logProgress("Включена медленная анимация для лучшей визуализации");
        } else {
            logProgress("Headless режим - максимальная скорость выполнения");
        }

        return playwright.chromium().launch(options);
    }

    /**
     * Создает новую страницу с настроенными заголовками
     */
    public Page createPage() {
        Page page = browser.newPage();

        // Устанавливаем случайный User-Agent
        String userAgent = ScrapingConfig.USER_AGENTS[random.nextInt(ScrapingConfig.USER_AGENTS.length)];
        page.setExtraHTTPHeaders(Map.of("User-Agent", userAgent));

        log.debug("Created new page with User-Agent: {} (headless: {})", userAgent, headless);
        return page;
    }

    /**
     * Получает текущий режим браузера
     */
    public boolean isHeadless() {
        return headless;
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