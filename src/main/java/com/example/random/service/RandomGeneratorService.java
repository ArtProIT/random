package com.example.random.service;

import com.example.random.exception.LeetCodeExceptions.ValidationException;
import com.example.random.model.ProblemDifficulty;
import com.example.random.model.ProblemInfo;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ���������� ������ ��� ��������� ��������� ����� � ������������ � ����������� �� ���������
 */
public class RandomGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(RandomGeneratorService.class);

    /**
     * ���������� ��������� ����� � �������� ���������, �������� ��������� �����
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
     * ���������� ��������� ����� �� ����� � ������ �������� �� ���������
     */
    public Integer generateRandomProblemNumber(int min, int max, Set<Integer> excludeSet,
                                               Set<ProblemDifficulty> allowedDifficulties,
                                               Map<String, ProblemInfo> allProblems) throws ValidationException {
        validateRange(min, max);
        validateDifficulties(allowedDifficulties);

        if (allProblems == null || allProblems.isEmpty()) {
            throw new ValidationException("������ ����� �� ����� ���� ������");
        }

        // �������� ��� ������ � ��������� � ������ ����������
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
     * �������� ���������� �� ��������� ������� � ������ ��������
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
     * ������ ������ � �������, ������������ ��������
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
     * ������������ ��������� ����� � ������, ����������� ��������
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
            throw new ValidationException("����������� �������� �� ����� ���� ������ �������������");
        }
        if (min < 1) {
            throw new ValidationException("����������� �������� ������ ���� ������ 0");
        }
    }

    private void validateDifficulties(Set<ProblemDifficulty> difficulties) throws ValidationException {
        if (difficulties == null || difficulties.isEmpty()) {
            throw new ValidationException("������ ���� ������ ���� �� ���� ������� ���������");
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
            logger.warn("������������ �����: {}", value);
            return null;
        }
    }

    /**
     * ����� ��� �������� ���������� �� �������
     */
    @Value
    public static class ProblemStatistics {
        long total;
        long easy;
        long medium;
        long hard;

        @Override
        public String toString() {
            return String.format("Total: %d (Easy: %d, Medium: %d, Hard: %d)",
                    total, easy, medium, hard);
        }
    }
}