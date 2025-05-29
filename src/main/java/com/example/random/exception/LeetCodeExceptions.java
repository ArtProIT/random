package com.example.random.exception;

/**
 * Кастомные исключения для работы с LeetCode
 */
public class LeetCodeExceptions {

    public static class LeetCodeScrapingException extends Exception {
        public LeetCodeScrapingException(String message) {
            super(message);
        }

        public LeetCodeScrapingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ApiDataException extends LeetCodeScrapingException {
        public ApiDataException(String message, Throwable cause) {
            super("Failed to fetch API data: " + message, cause);
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}