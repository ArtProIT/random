package com.example.random;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RandomNumberApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RandomNumberApp::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("Генератор случайного числа");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLayout(new GridLayout(6, 1, 10, 5));

        JTextField minField = new JTextField();
        JTextField maxField = new JTextField();
        JTextField excludeField = new JTextField();
        JLabel resultLabel = new JLabel("Результат: ", SwingConstants.CENTER);
        JButton generateButton = new JButton("Сгенерировать");

        frame.add(new JLabel("Значени от:"));
        frame.add(minField);
        frame.add(new JLabel("Значение до:"));
        frame.add(maxField);
        frame.add(new JLabel("Исключить (через запятую):"));
        frame.add(excludeField);
        frame.add(generateButton);
        frame.add(resultLabel);

        generateButton.addActionListener((ActionEvent e) -> {
            try {
                int min = Integer.parseInt(minField.getText().trim());
                int max = Integer.parseInt(maxField.getText().trim());
                String[] excludeStrings = excludeField.getText().split(",");
                Set<Integer> excludeSet = Arrays.stream(excludeStrings)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toSet());

                List<Integer> possible = new ArrayList<>();
                for (int i = min; i <= max; i++) {
                    if (!excludeSet.contains(i)) {
                        possible.add(i);
                    }
                }

                if (possible.isEmpty()) {
                    resultLabel.setText("Нет доступных чисел.");
                } else {
                    Collections.shuffle(possible);
                    resultLabel.setText("Результат: " + possible.get(0));
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("Ошибка ввода!");
            }
        });

        frame.setVisible(true);
    }
}
