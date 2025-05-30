package com.example.random.scraper.pages;

import com.example.random.config.ScrapingConfig;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Базовый класс для всех Page Objects
 */
@Slf4j
public abstract class BasePage {
    protected final Page page;
    protected Consumer<String> progressCallback;

    public BasePage(Page page) {
        this.page = page;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    protected void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        log.info(message);
    }

    /**
     * Переходит по URL и ожидает полной загрузки страницы
     */
    protected void navigateAndWait(String url) {
        navigateAndWait(url, ScrapingConfig.DEFAULT_WAIT_TIME);
    }

    /**
     * Переходит по URL и ожидает полной загрузки страницы с указанной задержкой
     */
    protected void navigateAndWait(String url, long additionalWaitMs) {
        try {
            logProgress("Переходим на: " + url);
            page.navigate(url);

            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForFunction("() => document.readyState === 'complete'");

            if (additionalWaitMs > 0) {
                Thread.sleep(additionalWaitMs);
            }

            logProgress("Страница загружена успешно");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Navigation interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to: " + url, e);
        }
    }

    /**
     * Пытается кликнуть по элементу из списка селекторов
     */
    protected boolean tryClickAnySelector(String[] selectors, String description) {
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    logProgress("Найден элемент " + description + ": " + selector);
                    page.click(selector);
                    return true;
                }
            } catch (Exception e) {
                log.debug("Selector {} failed: {}", selector, e.getMessage());
            }
        }

        logProgress(description + " не найден");
        return false;
    }

    /**
     * Выполняет JavaScript код на странице
     */
    protected Object executeScript(String script) {
        try {
            return page.evaluate(script);
        } catch (Exception e) {
            log.error("Script execution failed: {}", script, e);
            throw new RuntimeException("Script execution failed", e);
        }
    }

    /**
     * Получает текст всей страницы
     */
    protected String getPageText() {
        return (String) executeScript("() => document.body.innerText");
    }
}