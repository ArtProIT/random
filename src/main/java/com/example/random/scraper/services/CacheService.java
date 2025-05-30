package com.example.random.scraper.services;

import com.example.random.model.ProblemInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

/**
 * ������ ��� ���������� ������������ ������
 */
@Slf4j
public class CacheService {
    private static final long DEFAULT_CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000;

    private CacheEntry<Map<String, ProblemInfo>> problemsCache;
    @Getter private final long cacheExpiryTime;
    private Consumer<String> progressCallback;

    public CacheService() {
        this(DEFAULT_CACHE_EXPIRY_TIME);
    }

    public CacheService(long cacheExpiryTimeMs) {
        this.cacheExpiryTime = cacheExpiryTimeMs;
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

    /**
     * ��������� ���������� � ������� � ���
     */
    public void cacheProblems(Map<String, ProblemInfo> problems) {
        if (problems == null || problems.isEmpty()) {
            log.warn("Attempted to cache empty problems data");
            return;
        }

        problemsCache = new CacheEntry<>(problems, System.currentTimeMillis());
        logProgress("������ � ������� ��������� � ���: " + problems.size() + " �����");
    }

    /**
     * �������� ���������� � ������� �� ����
     */
    public Map<String, ProblemInfo> getCachedProblems() {
        if (problemsCache == null) {
            logProgress("��� ����");
            return null;
        }

        if (isExpired(problemsCache)) {
            logProgress("��� �������, ������� ������");
            problemsCache = null;
            return null;
        }

        logProgress("���������� ������������ ������ � �������");
        return problemsCache.data;
    }

    /**
     * ������� ���� ���
     */
    public void clearCache() {
        problemsCache = null;
        logProgress("��� ������");
    }

    /**
     * ���������, ����� �� ���� �������� ������ � ����
     */
    private boolean isExpired(CacheEntry<?> entry) {
        return (System.currentTimeMillis() - entry.timestamp) > cacheExpiryTime;
    }

    /**
     * �������� ���������� ����
     */
    public CacheStats getStats() {
        if (problemsCache == null) {
            return new CacheStats(false, 0, 0, 0);
        }

        boolean isValid = !isExpired(problemsCache);
        int size = problemsCache.data.size();
        long age = System.currentTimeMillis() - problemsCache.timestamp;

        return new CacheStats(isValid, size, problemsCache.timestamp, age);
    }

    /**
     * �������������� ����� ��� �������� ������ � ����
     */
    @AllArgsConstructor
    @Getter
    private static class CacheEntry<T> {
        final T data;
        final long timestamp;
    }

    /**
     * ���������� ����
     */
    @AllArgsConstructor
    @Getter
    public static class CacheStats {
        private final boolean valid;
        private final int size;
        private final long lastUpdate;
        private final long age;

        public String getFormattedAge() {
            if (age < 1000) return age + "ms";
            if (age < 60 * 1000) return (age / 1000) + "s";
            if (age < 60 * 60 * 1000) return (age / (60 * 1000)) + "m";
            return (age / (60 * 60 * 1000)) + "h";
        }

        @Override
        public String toString() {
            return String.format("Cache[valid=%s, size=%d, age=%s]",
                    valid, size, getFormattedAge());
        }
    }
}