package com.example.random.scraper.browser;

import com.example.random.config.ScrapingConfig;
import com.example.random.exception.LeetCodeExceptions.LeetCodeScrapingException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ��������� ������� ��������� ������� ��� �������� ���������
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
     * ��������� �������� � ���������� ���������
     */
    public <T> T executeWithRetry(
            Supplier<T> operation,
            String operationName,
            ResultValidator<T> validator) throws LeetCodeScrapingException {

        int attempts = 0;
        Exception lastException = null;

        while (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
            attempts++;
            logProgress("������� " + attempts + " ����������: " + operationName);

            try {
                T result = operation.get();

                if (validator.isValid(result)) {
                    logProgress(operationName + " ��������� ������� �� ������� " + attempts);
                    return result;
                }

                logProgress("��������� �� ������ ��������� �� ������� " + attempts);

            } catch (Exception e) {
                lastException = e;
                logProgress("������ � ������� " + attempts + ": " + e.getMessage());
            }

            // ���� ����� ��������� ��������
            if (attempts < ScrapingConfig.MAX_RETRY_ATTEMPTS) {
                waitBetweenRetries(attempts);
            }
        }

        String errorMessage = String.format(
                "�������� '%s' �� ������� ����� %d �������",
                operationName, ScrapingConfig.MAX_RETRY_ATTEMPTS
        );

        throw new LeetCodeScrapingException(errorMessage, lastException);
    }

    /**
     * ��������� �������� � ���������� ��������� (���������� ������)
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) throws LeetCodeScrapingException {
        return executeWithRetry(operation, operationName, result -> result != null);
    }

    /**
     * �������� ����� ��������� � ���������������� ���������
     */
    private void waitBetweenRetries(int attemptNumber) {
        try {
            long delay = ScrapingConfig.RETRY_DELAY * (long) Math.pow(2, attemptNumber - 1);
            delay = Math.min(delay, 10000);

            logProgress("������� " + delay + "�� ����� ��������� ��������...");
            Thread.sleep(delay);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }

    /**
     * ��������� ��� ��������� �����������
     */
    @FunctionalInterface
    public interface ResultValidator<T> {
        boolean isValid(T result);
    }
}