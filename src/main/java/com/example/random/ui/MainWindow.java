package com.example.random.ui;

import com.example.random.exception.LeetCodeExceptions.*;
import com.example.random.model.ProblemDifficulty;
import com.example.random.model.ProblemInfo;
import com.example.random.service.LeetCodeService;
import com.example.random.service.RandomGeneratorService;
import com.example.random.service.RandomGeneratorService.ProblemStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Улучшенное главное окно приложения с улучшенной логикой UI
 */
public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private static final String DEFAULT_USERNAME = "ArtProIT";
    private static final String FETCH_BUTTON_DEFAULT_TEXT = "Получить решенные задачи с LeetCode";
    private static final String FETCH_BUTTON_LOADING_TEXT = "Получение данных...";
    private static final String LOAD_PROBLEMS_BUTTON_TEXT = "Загрузить все задачи";
    private static final String LOAD_PROBLEMS_LOADING_TEXT = "Загрузка задач...";

    private final RandomGeneratorService randomService;
    private final LeetCodeService leetCodeService;

    // Основные компоненты UI
    private JFrame frame;
    private JTextField profileField;
    private JTextField minField;
    private JTextField maxField;
    private JTextField excludeField;
    private JLabel resultLabel;
    private JLabel statisticsLabel;
    private JButton fetchButton;
    private JButton loadProblemsButton;
    private JButton generateButton;
    private JTextArea progressArea;
    private JScrollPane progressScrollPane;

    // Компоненты для фильтрации по сложности
    private JCheckBox easyCheckBox;
    private JCheckBox mediumCheckBox;
    private JCheckBox hardCheckBox;
    private JPanel difficultyPanel;
    private JLabel difficultyHintLabel;

    // Состояние приложения
    private Map<String, ProblemInfo> allProblemsInfo = new HashMap<>();
    private boolean problemsLoaded = false;

    public MainWindow(RandomGeneratorService randomService, LeetCodeService leetCodeService) {
        this.randomService = randomService;
        this.leetCodeService = leetCodeService;
        initializeComponents();
        setupEventHandlers();
        updateUIState(); // Инициализация состояния UI
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initializeComponents() {
        frame = new JFrame("Генератор случайного числа LeetCode - Улучшенная версия");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 700);
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
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Параметры"));

        // Основные параметры
        JPanel mainParams = new JPanel(new GridLayout(5, 2, 5, 5));

        profileField = new JTextField(DEFAULT_USERNAME);
        minField = new JTextField("1");
        maxField = new JTextField("3000");
        excludeField = new JTextField();
        resultLabel = new JLabel("Результат: ", SwingConstants.CENTER);
        statisticsLabel = new JLabel("Статистика: не загружено", SwingConstants.CENTER);

        mainParams.add(new JLabel("Профиль LeetCode (username):"));
        mainParams.add(profileField);
        mainParams.add(new JLabel("Минимальное значение:"));
        mainParams.add(minField);
        mainParams.add(new JLabel("Максимальное значение:"));
        mainParams.add(maxField);
        mainParams.add(new JLabel("Исключить (через запятую):"));
        mainParams.add(excludeField);
        mainParams.add(new JLabel(""));
        mainParams.add(resultLabel);

        // Панель сложности
        difficultyPanel = createDifficultyPanel();

        // Статистика
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.add(statisticsLabel);

        panel.add(mainParams, BorderLayout.CENTER);
        panel.add(difficultyPanel, BorderLayout.SOUTH);
        panel.add(statsPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Уровни сложности"));

        // Панель с чекбоксами
        JPanel checkboxPanel = new JPanel(new FlowLayout());

        easyCheckBox = new JCheckBox("Easy", false); // По умолчанию отключены
        mediumCheckBox = new JCheckBox("Medium", false);
        hardCheckBox = new JCheckBox("Hard", false);

        easyCheckBox.setForeground(new Color(0, 150, 0)); // Зеленый
        mediumCheckBox.setForeground(new Color(255, 165, 0)); // Оранжевый
        hardCheckBox.setForeground(new Color(220, 20, 60)); // Красный

        // Добавляем слушатели для обновления статистики и состояния UI
        easyCheckBox.addActionListener(e -> {
            updateStatistics();
            updateUIState();
        });
        mediumCheckBox.addActionListener(e -> {
            updateStatistics();
            updateUIState();
        });
        hardCheckBox.addActionListener(e -> {
            updateStatistics();
            updateUIState();
        });

        checkboxPanel.add(easyCheckBox);
        checkboxPanel.add(mediumCheckBox);
        checkboxPanel.add(hardCheckBox);

        // Подсказка
        difficultyHintLabel = new JLabel(
                "<html><i>Сначала загрузите все задачи для использования фильтрации по сложности</i></html>",
                SwingConstants.CENTER
        );
        difficultyHintLabel.setForeground(Color.GRAY);

        panel.add(checkboxPanel, BorderLayout.CENTER);
        panel.add(difficultyHintLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        fetchButton = new JButton(FETCH_BUTTON_DEFAULT_TEXT);
        loadProblemsButton = new JButton(LOAD_PROBLEMS_BUTTON_TEXT);
        generateButton = new JButton("Сгенерировать");

        fetchButton.setPreferredSize(new Dimension(280, 35));
        loadProblemsButton.setPreferredSize(new Dimension(180, 35));
        generateButton.setPreferredSize(new Dimension(150, 35));

        // Добавляем подсказки
        fetchButton.setToolTipText("Получить список решенных задач пользователя и добавить их в исключения");
        loadProblemsButton.setToolTipText("Загрузить полную информацию о всех задачах для фильтрации по сложности");
        generateButton.setToolTipText("Сгенерировать случайное число с учетом всех фильтров");

        panel.add(fetchButton);
        panel.add(loadProblemsButton);
        panel.add(generateButton);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ход выполнения"));

        progressArea = new JTextArea(12, 60);
        progressArea.setEditable(false);
        progressArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        progressArea.setBackground(new Color(248, 248, 248));
        progressArea.setText("Готов к работе...\n" +
                "1. Нажмите 'Загрузить все задачи' для активации фильтрации по сложности\n" +
                "2. Выберите уровни сложности (Easy/Medium/Hard)\n" +
                "3. Нажмите 'Сгенерировать' для получения случайного номера\n");

        progressScrollPane = new JScrollPane(progressArea);
        progressScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(progressScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventHandlers() {
        fetchButton.addActionListener(this::onFetchButtonClick);
        loadProblemsButton.addActionListener(this::onLoadProblemsButtonClick);
        generateButton.addActionListener(this::onGenerateButtonClick);
    }

    /**
     * Обновляет состояние UI элементов в зависимости от загруженных данных
     */
    private void updateUIState() {
        // Состояние чекбоксов сложности
        boolean difficultyEnabled = problemsLoaded;
        easyCheckBox.setEnabled(difficultyEnabled);
        mediumCheckBox.setEnabled(difficultyEnabled);
        hardCheckBox.setEnabled(difficultyEnabled);

        // Обновляем подсказку
        if (problemsLoaded) {
            difficultyHintLabel.setText(
                    "<html><i>Выберите один или несколько уровней сложности для фильтрации</i></html>"
            );
            difficultyHintLabel.setForeground(new Color(0, 100, 0));
        } else {
            difficultyHintLabel.setText(
                    "<html><i>Сначала загрузите все задачи для использования фильтрации по сложности</i></html>"
            );
            difficultyHintLabel.setForeground(Color.GRAY);
        }

        // Состояние кнопки генерации
        boolean canGenerate = true;
        String generateTooltip = "Сгенерировать случайное число";

        if (problemsLoaded) {
            Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();
            if (selectedDifficulties.isEmpty()) {
                canGenerate = false;
                generateTooltip = "Выберите хотя бы один уровень сложности для генерации";
            } else {
                generateTooltip = "Сгенерировать случайное число с фильтрацией по сложности: " + selectedDifficulties;
            }
        } else {
            generateTooltip = "Сгенерировать случайное число (без фильтрации по сложности)";
        }

        generateButton.setEnabled(canGenerate);
        generateButton.setToolTipText(generateTooltip);

        // Обновляем цвет кнопки в зависимости от состояния
        if (canGenerate) {
            generateButton.setBackground(null); // Стандартный цвет
        } else {
            generateButton.setBackground(new Color(255, 200, 200)); // Светло-красный
        }
    }

    private void onFetchButtonClick(ActionEvent e) {
        String username = profileField.getText().trim();

        if (!leetCodeService.isValidUsername(username)) {
            showErrorMessage("Введите корректный username профиля LeetCode");
            return;
        }

        clearProgress();
        addProgressStep("Начинаем получение данных с LeetCode для пользователя: " + username);
        setFetchButtonLoading(true);

        SwingWorker<Set<Integer>, Void> worker = new SwingWorker<Set<Integer>, Void>() {
            @Override
            protected Set<Integer> doInBackground() throws Exception {
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

    private void onLoadProblemsButtonClick(ActionEvent e) {
        clearProgress();
        addProgressStep("Начинаем загрузку информации о всех задачах...");
        addProgressStep("Это может занять некоторое время...");
        setLoadProblemsButtonLoading(true);

        SwingWorker<Map<String, ProblemInfo>, Void> worker = new SwingWorker<Map<String, ProblemInfo>, Void>() {
            @Override
            protected Map<String, ProblemInfo> doInBackground() throws Exception {
                return leetCodeService.getAllProblemsInfo(MainWindow.this::addProgressStep);
            }

            @Override
            protected void done() {
                try {
                    Map<String, ProblemInfo> problems = get();
                    handleLoadProblemsResult(problems);
                } catch (Exception ex) {
                    handleLoadProblemsError(ex);
                } finally {
                    setLoadProblemsButtonLoading(false);
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

            addProgressStep("Диапазон: [" + min + ", " + max + "]");

            addProgressStep("Парсим список исключений...");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            addProgressStep("Найдено исключений: " + excludeSet.size());

            Integer result;

            if (!problemsLoaded) {
                addProgressStep("Используем простую генерацию (фильтрация по сложности отключена)...");
                result = randomService.generateRandomNumber(min, max, excludeSet);
            } else {
                // Проверяем какие уровни сложности выбраны
                Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();

                if (selectedDifficulties.isEmpty()) {
                    addProgressStep("Ошибка: не выбран ни один уровень сложности!");
                    showErrorMessage("Выберите хотя бы один уровень сложности (Easy/Medium/Hard)");
                    return;
                }

                addProgressStep("Выбранные уровни сложности: " + selectedDifficulties);
                addProgressStep("Используем генерацию с фильтрацией по сложности...");
                result = randomService.generateRandomProblemNumber(min, max, excludeSet, selectedDifficulties, allProblemsInfo);

                // Показываем информацию о выбранной задаче
                if (result != null) {
                    showProblemInfo(result);
                }
            }

            if (result == null) {
                addProgressStep("Нет доступных чисел для генерации с заданными параметрами");
                resultLabel.setText("Нет доступных чисел.");
            } else {
                addProgressStep("Успешно сгенерировано число: " + result);
                resultLabel.setText("Результат: " + result);
                resultLabel.setForeground(new Color(0, 150, 0)); // Зеленый цвет для результата
            }

        } catch (ValidationException ex) {
            addProgressStep("Ошибка валидации: " + ex.getMessage());
            resultLabel.setText("Ошибка: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
        } catch (NumberFormatException ex) {
            addProgressStep("Ошибка ввода: " + ex.getMessage());
            resultLabel.setText("Ошибка ввода: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
        } catch (Exception ex) {
            addProgressStep("Неожиданная ошибка: " + ex.getMessage());
            resultLabel.setText("Ошибка: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
            logger.error("Unexpected error during generation", ex);
        }
    }

    private void handleFetchResult(Set<Integer> solvedProblems) {
        if (solvedProblems != null && !solvedProblems.isEmpty()) {
            addProgressStep("Успешно получено " + solvedProblems.size() + " решенных задач");
            addProgressStep("Заполняем поле исключений...");

            String excludeText = randomService.formatExcludeNumbers(solvedProblems);
            excludeField.setText(excludeText);

            addProgressStep("Готово! Данные загружены и готовы к использованию");
            updateStatistics();
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
        logger.error("Error fetching solved problems", ex);
    }

    private void handleLoadProblemsResult(Map<String, ProblemInfo> problems) {
        if (problems != null && !problems.isEmpty()) {
            allProblemsInfo = problems;
            problemsLoaded = true;

            addProgressStep("Успешно загружено " + problems.size() + " задач");
            addProgressStep("Фильтрация по сложности теперь доступна!");
            addProgressStep("Активируем чекбоксы уровней сложности...");

            // Автоматически выбираем все уровни сложности после загрузки
            easyCheckBox.setSelected(true);
            mediumCheckBox.setSelected(true);
            hardCheckBox.setSelected(true);

            updateUIState();
            updateStatistics();
            showInfoMessage("Загружено " + problems.size() + " задач. Фильтрация по сложности активна!");
        } else {
            addProgressStep("Не удалось загрузить информацию о задачах");
            showErrorMessage("Не удалось загрузить информацию о задачах");
        }
    }

    private void handleLoadProblemsError(Exception ex) {
        addProgressStep("Ошибка при загрузке задач: " + ex.getMessage());
        addProgressStep("Попробуйте еще раз или проверьте соединение с интернетом");
        showErrorMessage("Ошибка при загрузке задач: " + ex.getMessage());
        logger.error("Error loading problems info", ex);
    }

    private Set<ProblemDifficulty> getSelectedDifficulties() {
        Set<ProblemDifficulty> selected = new HashSet<>();

        if (easyCheckBox.isSelected()) {
            selected.add(ProblemDifficulty.EASY);
        }
        if (mediumCheckBox.isSelected()) {
            selected.add(ProblemDifficulty.MEDIUM);
        }
        if (hardCheckBox.isSelected()) {
            selected.add(ProblemDifficulty.HARD);
        }

        return selected;
    }

    private void updateStatistics() {
        if (!problemsLoaded || allProblemsInfo.isEmpty()) {
            statisticsLabel.setText("Статистика: загрузите задачи для просмотра статистики");
            return;
        }

        try {
            int min = parseInteger(minField.getText().trim(), "минимальное значение");
            int max = parseInteger(maxField.getText().trim(), "максимальное значение");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();

            if (selectedDifficulties.isEmpty()) {
                statisticsLabel.setText("Статистика: выберите хотя бы один уровень сложности");
                statisticsLabel.setForeground(Color.RED);
                return;
            }

            ProblemStatistics stats = randomService.getProblemStatistics(min, max, excludeSet, selectedDifficulties, allProblemsInfo);

            String statsText = String.format("Доступно задач: %d (Easy: %d, Medium: %d, Hard: %d)",
                    stats.getTotal(), stats.getEasy(), stats.getMedium(), stats.getHard());
            statisticsLabel.setText(statsText);
            statisticsLabel.setForeground(new Color(0, 100, 0)); // Темно-зеленый

        } catch (Exception e) {
            statisticsLabel.setText("Статистика: ошибка в параметрах");
            statisticsLabel.setForeground(Color.RED);
        }
    }

    /**
     * Исправленный метод для отображения информации о задаче
     */
    private void showProblemInfo(Integer problemNumber) {
        if (problemNumber == null || allProblemsInfo.isEmpty()) {
            return;
        }

        // Находим информацию о задаче по номеру
        Optional<ProblemInfo> problemInfo = allProblemsInfo.values().stream()
                .filter(p -> Objects.equals(p.getNumber(), problemNumber))
                .findFirst();

        if (problemInfo.isPresent()) {
            ProblemInfo info = problemInfo.get();
            addProgressStep(String.format("Задача: #%d - %s [%s]",
                    info.getNumber(), info.getTitle(), info.getDifficulty()));
        } else {
            addProgressStep("Информация о задаче #" + problemNumber + " не найдена");
        }
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

    private void setLoadProblemsButtonLoading(boolean loading) {
        loadProblemsButton.setEnabled(!loading);
        loadProblemsButton.setText(loading ? LOAD_PROBLEMS_LOADING_TEXT : LOAD_PROBLEMS_BUTTON_TEXT);

        // Блокируем чекбоксы во время загрузки
        if (loading) {
            easyCheckBox.setEnabled(false);
            mediumCheckBox.setEnabled(false);
            hardCheckBox.setEnabled(false);
        } else {
            updateUIState(); // Восстанавливаем состояние после загрузки
        }
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