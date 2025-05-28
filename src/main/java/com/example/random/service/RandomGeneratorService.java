package com.example.random.service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для генерации случайных чисел с исключениями
 */
public class RandomGeneratorService {

    /**
     * Генерирует случайное число в заданном диапазоне, исключая указанные числа
     */
    public Integer generateRandomNumber(int min, int max, Set<Integer> excludeSet) {
        List<Integer> possibleNumbers = createPossibleNumbers(min, max, excludeSet);

        if (possibleNumbers.isEmpty()) {
            return null;
        }

        Collections.shuffle(possibleNumbers);
        return possibleNumbers.get(0);
    }

    /**
     * Парсит строку с числами, разделенными запятыми
     */
    public Set<Integer> parseExcludeNumbers(String excludeText) {
        if (excludeText == null || excludeText.trim().isEmpty()) {
            return new HashSet<>();
        }

        String[] excludeStrings = excludeText.split(",");
        return Arrays.stream(excludeStrings)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parseInteger)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Конвертирует множество чисел в строку, разделенную запятыми
     */
    public String formatExcludeNumbers(Set<Integer> numbers) {
        return numbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
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
            System.err.println("Некорректное число: " + value);
            return null;
        }
    }
}