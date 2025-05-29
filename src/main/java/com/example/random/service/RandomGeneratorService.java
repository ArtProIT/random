package com.example.random.service;

import com.example.random.exception.LeetCodeExceptions.ValidationException;
import com.example.random.model.ProblemDifficulty;
import com.example.random.model.ProblemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Улучшенный сервис для генерации случайных чисел с исключениями и фильтрацией по сложности
 */
public class RandomGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(RandomGeneratorService.class);

    /**
     * Генерирует случайное число в заданном диапазоне, исключая указанные числа
     */
    public Integer generateRandomNumber(int min, int max, Set<Integer> excludeSet) throws ValidationException {
        validateRange(min, max);

        List<Integer> possibleNumbers = createPossibleNumbers(min, max, excludeSet);

        if (possibleNumbers.isEmpty()) {
            logger.warn("No possible numbers in range [{}, {}] excluding {}", min, max, excludeSet);
            return null;
        }

        Collections.shuffle(possibleNumbers);
        Integer result = possibleNumbers.get(0);

        logger.info("Generated random number: {} from range [{}, {}]", result, min, max);
        return result;
    }

    /**
     * Генерирует случайное число из задач с учетом фильтров по сложности
     */
    public Integer generateRandomProblemNumber(int min, int max, Set<Integer> excludeSet,
                                               Set<ProblemDifficulty> allowedDifficulties,
                                               Map<String, ProblemInfo> allProblems) throws ValidationException {
        validateRange(min, max);
        validateDifficulties(allowedDifficulties);

        if (allProblems == null || allProblems.isEmpty()) {
            throw new ValidationException("Список задач не может быть пустым");
        }

        // Получаем все задачи в диапазоне с нужной сложностью
        List<Integer> possibleNumbers = allProblems.values().stream()
                .filter(problem -> problem.getNumber() >= min && problem.getNumber() <= max)
                .filter(problem -> allowedDifficulties.contains(problem.getDifficulty()))
                .filter(problem -> !excludeSet.contains(problem.getNumber()))
                .map(ProblemInfo::getNumber)
                .collect(Collectors.toList());

        if (possibleNumbers.isEmpty()) {
            logger.warn("No possible problems in range [{}, {}] with difficulties {} excluding {}",
                    min, max, allowedDifficulties, excludeSet);
            return null;
        }

        Collections.shuffle(possibleNumbers);
        Integer result = possibleNumbers.get(0);

        logger.info("Generated random problem number: {} from range [{}, {}] with difficulties {}",
                result, min, max, allowedDifficulties);
        return result;
    }

    /**
     * Получает статистику по доступным задачам с учетом фильтров
     */
    public ProblemStatistics getProblemStatistics(int min, int max, Set<Integer> excludeSet,
                                                  Set<ProblemDifficulty> allowedDifficulties,
                                                  Map<String, ProblemInfo> allProblems) {
        if (allProblems == null || allProblems.isEmpty()) {
            return new ProblemStatistics(0, 0, 0, 0);
        }

        Map<ProblemDifficulty, Long> difficultyCounts = allProblems.values().stream()
                .filter(problem -> problem.getNumber() >= min && problem.getNumber() <= max)
                .filter(problem -> allowedDifficulties.contains(problem.getDifficulty()))
                .filter(problem -> !excludeSet.contains(problem.getNumber()))
                .collect(Collectors.groupingBy(ProblemInfo::getDifficulty, Collectors.counting()));

        long easyCount = difficultyCounts.getOrDefault(ProblemDifficulty.EASY, 0L);
        long mediumCount = difficultyCounts.getOrDefault(ProblemDifficulty.MEDIUM, 0L);
        long hardCount = difficultyCounts.getOrDefault(ProblemDifficulty.HARD, 0L);
        long totalCount = easyCount + mediumCount + hardCount;

        return new ProblemStatistics(totalCount, easyCount, mediumCount, hardCount);
    }

    /**
     * Парсит строку с числами, разделенными запятыми
     */
    public Set<Integer> parseExcludeNumbers(String excludeText) {
        if (excludeText == null || excludeText.trim().isEmpty()) {
            return new HashSet<>();
        }

        String[] excludeStrings = excludeText.split(",");
        Set<Integer> result = Arrays.stream(excludeStrings)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parseInteger)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        logger.debug("Parsed exclude numbers: {}", result);
        return result;
    }

    /**
     * Конвертирует множество чисел в строку, разделенную запятыми
     */
    public String formatExcludeNumbers(Set<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return "";
        }

        String result = numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        logger.debug("Formatted exclude numbers: {}", result);
        return result;
    }

    private void validateRange(int min, int max) throws ValidationException {
        if (min > max) {
            throw new ValidationException("Минимальное значение не может быть больше максимального");
        }
        if (min < 1) {
            throw new ValidationException("Минимальное значение должно быть больше 0");
        }
    }

    private void validateDifficulties(Set<ProblemDifficulty> difficulties) throws ValidationException {
        if (difficulties == null || difficulties.isEmpty()) {
            throw new ValidationException("Должен быть выбран хотя бы один уровень сложности");
        }
    }

    private List<Integer> createPossibleNumbers(int min, int max, Set<Integer> excludeSet) {
        List<Integer> possible = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            if (!excludeSet.contains(i)) {
                possible.add(i);
            }
        }
        return possible;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Некорректное число: {}", value);
            return null;
        }
    }

    /**
     * Класс для хранения статистики по задачам
     */
    public static class ProblemStatistics {
        private final long total;
        private final long easy;
        private final long medium;
        private final long hard;

        public ProblemStatistics(long total, long easy, long medium, long hard) {
            this.total = total;
            this.easy = easy;
            this.medium = medium;
            this.hard = hard;
        }

        public long getTotal() { return total; }
        public long getEasy() { return easy; }
        public long getMedium() { return medium; }
        public long getHard() { return hard; }

        @Override
        public String toString() {
            return String.format("Total: %d (Easy: %d, Medium: %d, Hard: %d)",
                    total, easy, medium, hard);
        }
    }
}