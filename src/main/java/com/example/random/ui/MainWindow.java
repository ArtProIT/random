package com.example.random.ui;

import com.example.random.service.LeetCodeService;
import com.example.random.service.RandomGeneratorService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

/**
 * Главное окно приложения
 */
public class MainWindow {
    private static final String DEFAULT_USERNAME = "ArtProIT";
    private static final String FETCH_BUTTON_DEFAULT_TEXT = "Получить решенные задачи с LeetCode";
    private static final String FETCH_BUTTON_LOADING_TEXT = "Получение данных...";

    private final RandomGeneratorService randomService;
    private final LeetCodeService leetCodeService;

    private JFrame frame;
    private JTextField profileField;
    private JTextField minField;
    private JTextField maxField;
    private JTextField excludeField;
    private JLabel resultLabel;
    private JButton fetchButton;
    private JButton generateButton;
    private JTextArea progressArea;
    private JScrollPane progressScrollPane;

    public MainWindow(RandomGeneratorService randomService, LeetCodeService leetCodeService) {
        this.randomService = randomService;
        this.leetCodeService = leetCodeService;
        initializeComponents();
        setupEventHandlers();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initializeComponents() {
        frame = new JFrame("Генератор случайного числа LeetCode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        // Основная панель с полями ввода
        JPanel inputPanel = createInputPanel();

        // Панель с кнопками
        JPanel buttonPanel = createButtonPanel();

        // Панель прогресса
        JPanel progressPanel = createProgressPanel();

        // Компоновка
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(progressPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Параметры"));

        // Создаю компоненты
        profileField = new JTextField(DEFAULT_USERNAME);
        minField = new JTextField();
        maxField = new JTextField();
        excludeField = new JTextField();
        resultLabel = new JLabel("Результат: ", SwingConstants.CENTER);

        // Добавляю компоненты
        panel.add(new JLabel("Профиль LeetCode (username):"));
        panel.add(profileField);
        panel.add(new JLabel("Минимальное значение:"));
        panel.add(minField);
        panel.add(new JLabel("Максимальное значение:"));
        panel.add(maxField);
        panel.add(new JLabel("Исключить (через запятую):"));
        panel.add(excludeField);
        panel.add(new JLabel(""));
        panel.add(resultLabel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        fetchButton = new JButton(FETCH_BUTTON_DEFAULT_TEXT);
        generateButton = new JButton("Сгенерировать");

        fetchButton.setPreferredSize(new Dimension(250, 30));
        generateButton.setPreferredSize(new Dimension(150, 30));

        panel.add(fetchButton);
        panel.add(generateButton);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ход выполнения"));

        progressArea = new JTextArea(10, 50);
        progressArea.setEditable(false);
        progressArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        progressArea.setBackground(new Color(248, 248, 248));
        progressArea.setText("Готов к работе...\n");

        progressScrollPane = new JScrollPane(progressArea);
        progressScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(progressScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        fetchButton.addActionListener(this::onFetchButtonClick);
        generateButton.addActionListener(this::onGenerateButtonClick);
    }

    private void onFetchButtonClick(ActionEvent e) {
        String username = profileField.getText().trim();

        if (!leetCodeService.isValidUsername(username)) {
            showErrorMessage("Введите username профиля LeetCode");
            return;
        }

        clearProgress();
        addProgressStep("Начинаем получение данных с LeetCode...");
        setFetchButtonLoading(true);

        SwingWorker<Set<Integer>, Void> worker = new SwingWorker<Set<Integer>, Void>() {
            @Override
            protected Set<Integer> doInBackground() throws Exception {
                // Использую метод с callback для реального прогресса, интерактив
                return leetCodeService.getSolvedProblems(username, MainWindow.this::addProgressStep);
            }

            @Override
            protected void done() {
                try {
                    Set<Integer> solvedProblems = get();
                    handleFetchResult(solvedProblems);
                } catch (Exception ex) {
                    handleFetchError(ex);
                } finally {
                    setFetchButtonLoading(false);
                }
            }
        };

        worker.execute();
    }

    private void onGenerateButtonClick(ActionEvent e) {
        clearProgress();
        addProgressStep("Начинаем генерацию случайного числа...");

        try {
            addProgressStep("Проверяем введенные параметры...");
            int min = parseInteger(minField.getText().trim(), "минимальное значение");
            int max = parseInteger(maxField.getText().trim(), "максимальное значение");

            if (min > max) {
                addProgressStep("Ошибка: минимальное значение больше максимального");
                showErrorMessage("Минимальное значение не может быть больше максимального");
                return;
            }

            addProgressStep("Парсим список исключений...");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            addProgressStep("Найдено исключений: " + excludeSet.size());

            addProgressStep("Создаем список доступных чисел в диапазоне [" + min + ", " + max + "]...");
            Integer result = randomService.generateRandomNumber(min, max, excludeSet);

            if (result == null) {
                addProgressStep("Нет доступных чисел для генерации");
                resultLabel.setText("Нет доступных чисел.");
            } else {
                addProgressStep("Успешно сгенерировано число: " + result);
                resultLabel.setText("Результат: " + result);
            }

        } catch (NumberFormatException ex) {
            addProgressStep("Ошибка ввода: " + ex.getMessage());
            resultLabel.setText("Ошибка ввода: " + ex.getMessage());
        } catch (Exception ex) {
            addProgressStep("Неожиданная ошибка: " + ex.getMessage());
            resultLabel.setText("Ошибка: " + ex.getMessage());
        }
    }

    private void handleFetchResult(Set<Integer> solvedProblems) {
        if (solvedProblems != null && !solvedProblems.isEmpty()) {
            addProgressStep("Успешно получено " + solvedProblems.size() + " решенных задач");
            addProgressStep("Заполняем поле исключений...");

            String excludeText = randomService.formatExcludeNumbers(solvedProblems);
            excludeField.setText(excludeText);

            addProgressStep("Готово! Данные загружены и готовы к использованию");
            showInfoMessage("Найдено " + solvedProblems.size() + " решенных задач");
        } else {
            addProgressStep("Не удалось получить данные или задачи не найдены");
            addProgressStep("Проверьте правильность username и доступность профиля");
            showErrorMessage("Не удалось получить данные или задачи не найдены");
        }
    }

    private void handleFetchError(Exception ex) {
        addProgressStep("Произошла ошибка: " + ex.getMessage());
        addProgressStep("Попробуйте еще раз или проверьте соединение с интернетом");
        showErrorMessage("Ошибка при получении данных: " + ex.getMessage());
        ex.printStackTrace();
    }

    // Методы для работы с прогрессом
    private void clearProgress() {
        progressArea.setText("");
    }

    private void addProgressStep(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMessage = String.format("[%s] %s%n", timestamp, message);
            progressArea.append(formattedMessage);

            // Автоматическая прокрутка к последнему сообщению
            progressArea.setCaretPosition(progressArea.getDocument().getLength());
        });
    }

    private void setFetchButtonLoading(boolean loading) {
        fetchButton.setEnabled(!loading);
        fetchButton.setText(loading ? FETCH_BUTTON_LOADING_TEXT : FETCH_BUTTON_DEFAULT_TEXT);
    }

    private int parseInteger(String value, String fieldName) throws NumberFormatException {
        if (value.isEmpty()) {
            throw new NumberFormatException("Поле '" + fieldName + "' не может быть пустым");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Некорректное значение в поле '" + fieldName + "'");
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}