package com.example.random.scraper.browser;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * ��������� ��������� ������ �������� � ��������� �������
 */
@Slf4j
public class BrowserManager implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final Random random;
    private final boolean headless;
    private Consumer<String> progressCallback;

    /**
     * ����������� � ���������� headless ������
     */
    public BrowserManager(boolean headless) {
        this.headless = headless;
        this.random = new Random();
        this.playwright = Playwright.create();
        this.browser = createBrowser();
        log.info("Browser manager initialized (headless: {})", headless);
    }

    /**
     * ����������� �� ��������� (headless = true ��� �������� �������������)
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
        String modeText = headless ? "������� (headless)" : "�������";
        logProgress("��������� ������� Chromium � " + modeText + " ������...");

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(headless);

        // ��������� SlowMo ������ ��� ��-headless ������ ��� ������ ������������
        if (!headless) {
            options.setSlowMo(300);
            logProgress("�������� ��������� �������� ��� ������ ������������");
        } else {
            logProgress("Headless ����� - ������������ �������� ����������");
        }

        return playwright.chromium().launch(options);
    }

    /**
     * ������� ����� �������� � ������������ �����������
     */
    public Page createPage() {
        Page page = browser.newPage();

        // ������������� ��������� User-Agent
        String userAgent = ScrapingConfig.USER_AGENTS[random.nextInt(ScrapingConfig.USER_AGENTS.length)];
        page.setExtraHTTPHeaders(Map.of("User-Agent", userAgent));

        log.debug("Created new page with User-Agent: {} (headless: {})", userAgent, headless);
        return page;
    }

    /**
     * �������� ������� ����� ��������
     */
    public boolean isHeadless() {
        return headless;
    }

    @Override
    public void close() {
        try {
            if (browser != null && browser.isConnected()) {
                browser.close();
                logProgress("������� ������");
            }

            if (playwright != null) {
                playwright.close();
                logProgress("Playwright ������");
            }
        } catch (Exception e) {
            log.error("Error closing browser manager", e);
        }
    }
}