package com.example.random.scraper.browser;

import com.example.random.config.ScrapingConfig;
import com.example.random.exception.LeetCodeExceptions.LeetCodeScrapingException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Управляет логикой повторных попыток для операций скрапинга
 */
@Slf4j
public class RetryManager {
    private Consumer<String> progressCallback;

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void logProgress(String message) {
        if (progressCallback != null) {
            progressCallback.accept(message);
        }
        log.info(message);
    }

    /**
     * Выполняет операцию с повторными попытками
     */
    public <T> T executeWithRetry(
            Supplier<T> operation,
            String operationName,
            ResultValidator<T> validator) throws LeetCodeScrapingException {

        int attempts = 0;
        Exception lastException = null;

        while (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
            attempts++;
            logProgress("Попытка " + attempts + " выполнения: " + operationName);

            try {
                T result = operation.get();

                if (validator.isValid(result)) {
                    logProgress(operationName + " выполнено успешно на попытке " + attempts);
                    return result;
                }

                logProgress("Результат не прошел валидацию на попытке " + attempts);

            } catch (Exception e) {
                lastException = e;
                logProgress("Ошибка в попытке " + attempts + ": " + e.getMessage());
            }

            // Ждем перед следующей попыткой
            if (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
                waitBetweenRetries(attempts);
            }
        }

        String errorMessage = String.format(
                "Операция '%s' не удалась после %d попыток",
                operationName, ScrapingConfig.MAX_RETRY_ATTEMPTS
        );

        throw new LeetCodeScrapingException(errorMessage, lastException);
    }

    /**
     * Выполняет операцию с повторными попытками (упрощенная версия)
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) throws LeetCodeScrapingException {
        return executeWithRetry(operation, operationName, result -> result != null);
    }

    /**
     * Ожидание между попытками с экспоненциальной задержкой
     */
    private void waitBetweenRetries(int attemptNumber) {
        try {
            long delay = ScrapingConfig.RETRY_DELAY * (long) Math.pow(2, attemptNumber - 1);
            delay = Math.min(delay, 10000);

            logProgress("Ожидаем " + delay + "мс перед следующей попыткой...");
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }

    /**
     * Интерфейс для валидации результатов
     */
    @FunctionalInterface
    public interface ResultValidator<T> {
        boolean isValid(T result);
    }
}