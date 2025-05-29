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
 * ���������� ������� ���� ���������� � ���������� ������� UI
 */
public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

    private static final String DEFAULT_USERNAME = "ArtProIT";
    private static final String FETCH_BUTTON_DEFAULT_TEXT = "�������� �������� ������ � LeetCode";
    private static final String FETCH_BUTTON_LOADING_TEXT = "��������� ������...";
    private static final String LOAD_PROBLEMS_BUTTON_TEXT = "��������� ��� ������";
    private static final String LOAD_PROBLEMS_LOADING_TEXT = "�������� �����...";

    private final RandomGeneratorService randomService;
    private final LeetCodeService leetCodeService;

    // �������� ���������� UI
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

    // ���������� ��� ���������� �� ���������
    private JCheckBox easyCheckBox;
    private JCheckBox mediumCheckBox;
    private JCheckBox hardCheckBox;
    private JPanel difficultyPanel;
    private JLabel difficultyHintLabel;

    // ��������� ����������
    private Map<String, ProblemInfo> allProblemsInfo = new HashMap<>();
    private boolean problemsLoaded = false;

    public MainWindow(RandomGeneratorService randomService, LeetCodeService leetCodeService) {
        this.randomService = randomService;
        this.leetCodeService = leetCodeService;
        initializeComponents();
        setupEventHandlers();
        updateUIState(); // ������������� ��������� UI
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initializeComponents() {
        frame = new JFrame("��������� ���������� ����� LeetCode - ���������� ������");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 700);
        frame.setLayout(new BorderLayout());

        // �������� ������ � ������ �����
        JPanel inputPanel = createInputPanel();

        // ������ � ��������
        JPanel buttonPanel = createButtonPanel();

        // ������ ���������
        JPanel progressPanel = createProgressPanel();

        // ����������
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.add(progressPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("���������"));

        // �������� ���������
        JPanel mainParams = new JPanel(new GridLayout(5, 2, 5, 5));

        profileField = new JTextField(DEFAULT_USERNAME);
        minField = new JTextField("1");
        maxField = new JTextField("3000");
        excludeField = new JTextField();
        resultLabel = new JLabel("���������: ", SwingConstants.CENTER);
        statisticsLabel = new JLabel("����������: �� ���������", SwingConstants.CENTER);

        mainParams.add(new JLabel("������� LeetCode (username):"));
        mainParams.add(profileField);
        mainParams.add(new JLabel("����������� ��������:"));
        mainParams.add(minField);
        mainParams.add(new JLabel("������������ ��������:"));
        mainParams.add(maxField);
        mainParams.add(new JLabel("��������� (����� �������):"));
        mainParams.add(excludeField);
        mainParams.add(new JLabel(""));
        mainParams.add(resultLabel);

        // ������ ���������
        difficultyPanel = createDifficultyPanel();

        // ����������
        JPanel statsPanel = new JPanel(new FlowLayout());
        statsPanel.add(statisticsLabel);

        panel.add(mainParams, BorderLayout.CENTER);
        panel.add(difficultyPanel, BorderLayout.SOUTH);
        panel.add(statsPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createDifficultyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("������ ���������"));

        // ������ � ����������
        JPanel checkboxPanel = new JPanel(new FlowLayout());

        easyCheckBox = new JCheckBox("Easy", false); // �� ��������� ���������
        mediumCheckBox = new JCheckBox("Medium", false);
        hardCheckBox = new JCheckBox("Hard", false);

        easyCheckBox.setForeground(new Color(0, 150, 0)); // �������
        mediumCheckBox.setForeground(new Color(255, 165, 0)); // ���������
        hardCheckBox.setForeground(new Color(220, 20, 60)); // �������

        // ��������� ��������� ��� ���������� ���������� � ��������� UI
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

        // ���������
        difficultyHintLabel = new JLabel(
                "<html><i>������� ��������� ��� ������ ��� ������������� ���������� �� ���������</i></html>",
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
        generateButton = new JButton("�������������");

        fetchButton.setPreferredSize(new Dimension(280, 35));
        loadProblemsButton.setPreferredSize(new Dimension(180, 35));
        generateButton.setPreferredSize(new Dimension(150, 35));

        // ��������� ���������
        fetchButton.setToolTipText("�������� ������ �������� ����� ������������ � �������� �� � ����������");
        loadProblemsButton.setToolTipText("��������� ������ ���������� � ���� ������� ��� ���������� �� ���������");
        generateButton.setToolTipText("������������� ��������� ����� � ������ ���� ��������");

        panel.add(fetchButton);
        panel.add(loadProblemsButton);
        panel.add(generateButton);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("��� ����������"));

        progressArea = new JTextArea(12, 60);
        progressArea.setEditable(false);
        progressArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        progressArea.setBackground(new Color(248, 248, 248));
        progressArea.setText("����� � ������...\n" +
                "1. ������� '��������� ��� ������' ��� ��������� ���������� �� ���������\n" +
                "2. �������� ������ ��������� (Easy/Medium/Hard)\n" +
                "3. ������� '�������������' ��� ��������� ���������� ������\n");

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
     * ��������� ��������� UI ��������� � ����������� �� ����������� ������
     */
    private void updateUIState() {
        // ��������� ��������� ���������
        boolean difficultyEnabled = problemsLoaded;
        easyCheckBox.setEnabled(difficultyEnabled);
        mediumCheckBox.setEnabled(difficultyEnabled);
        hardCheckBox.setEnabled(difficultyEnabled);

        // ��������� ���������
        if (problemsLoaded) {
            difficultyHintLabel.setText(
                    "<html><i>�������� ���� ��� ��������� ������� ��������� ��� ����������</i></html>"
            );
            difficultyHintLabel.setForeground(new Color(0, 100, 0));
        } else {
            difficultyHintLabel.setText(
                    "<html><i>������� ��������� ��� ������ ��� ������������� ���������� �� ���������</i></html>"
            );
            difficultyHintLabel.setForeground(Color.GRAY);
        }

        // ��������� ������ ���������
        boolean canGenerate = true;
        String generateTooltip = "������������� ��������� �����";

        if (problemsLoaded) {
            Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();
            if (selectedDifficulties.isEmpty()) {
                canGenerate = false;
                generateTooltip = "�������� ���� �� ���� ������� ��������� ��� ���������";
            } else {
                generateTooltip = "������������� ��������� ����� � ����������� �� ���������: " + selectedDifficulties;
            }
        } else {
            generateTooltip = "������������� ��������� ����� (��� ���������� �� ���������)";
        }

        generateButton.setEnabled(canGenerate);
        generateButton.setToolTipText(generateTooltip);

        // ��������� ���� ������ � ����������� �� ���������
        if (canGenerate) {
            generateButton.setBackground(null); // ����������� ����
        } else {
            generateButton.setBackground(new Color(255, 200, 200)); // ������-�������
        }
    }

    private void onFetchButtonClick(ActionEvent e) {
        String username = profileField.getText().trim();

        if (!leetCodeService.isValidUsername(username)) {
            showErrorMessage("������� ���������� username ������� LeetCode");
            return;
        }

        clearProgress();
        addProgressStep("�������� ��������� ������ � LeetCode ��� ������������: " + username);
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
        addProgressStep("�������� �������� ���������� � ���� �������...");
        addProgressStep("��� ����� ������ ��������� �����...");
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
        addProgressStep("�������� ��������� ���������� �����...");

        try {
            addProgressStep("��������� ��������� ���������...");
            int min = parseInteger(minField.getText().trim(), "����������� ��������");
            int max = parseInteger(maxField.getText().trim(), "������������ ��������");

            addProgressStep("��������: [" + min + ", " + max + "]");

            addProgressStep("������ ������ ����������...");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            addProgressStep("������� ����������: " + excludeSet.size());

            Integer result;

            if (!problemsLoaded) {
                addProgressStep("���������� ������� ��������� (���������� �� ��������� ���������)...");
                result = randomService.generateRandomNumber(min, max, excludeSet);
            } else {
                // ��������� ����� ������ ��������� �������
                Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();

                if (selectedDifficulties.isEmpty()) {
                    addProgressStep("������: �� ������ �� ���� ������� ���������!");
                    showErrorMessage("�������� ���� �� ���� ������� ��������� (Easy/Medium/Hard)");
                    return;
                }

                addProgressStep("��������� ������ ���������: " + selectedDifficulties);
                addProgressStep("���������� ��������� � ����������� �� ���������...");
                result = randomService.generateRandomProblemNumber(min, max, excludeSet, selectedDifficulties, allProblemsInfo);

                // ���������� ���������� � ��������� ������
                if (result != null) {
                    showProblemInfo(result);
                }
            }

            if (result == null) {
                addProgressStep("��� ��������� ����� ��� ��������� � ��������� �����������");
                resultLabel.setText("��� ��������� �����.");
            } else {
                addProgressStep("������� ������������� �����: " + result);
                resultLabel.setText("���������: " + result);
                resultLabel.setForeground(new Color(0, 150, 0)); // ������� ���� ��� ����������
            }

        } catch (ValidationException ex) {
            addProgressStep("������ ���������: " + ex.getMessage());
            resultLabel.setText("������: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
        } catch (NumberFormatException ex) {
            addProgressStep("������ �����: " + ex.getMessage());
            resultLabel.setText("������ �����: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
        } catch (Exception ex) {
            addProgressStep("����������� ������: " + ex.getMessage());
            resultLabel.setText("������: " + ex.getMessage());
            resultLabel.setForeground(Color.RED);
            logger.error("Unexpected error during generation", ex);
        }
    }

    private void handleFetchResult(Set<Integer> solvedProblems) {
        if (solvedProblems != null && !solvedProblems.isEmpty()) {
            addProgressStep("������� �������� " + solvedProblems.size() + " �������� �����");
            addProgressStep("��������� ���� ����������...");

            String excludeText = randomService.formatExcludeNumbers(solvedProblems);
            excludeField.setText(excludeText);

            addProgressStep("������! ������ ��������� � ������ � �������������");
            updateStatistics();
            showInfoMessage("������� " + solvedProblems.size() + " �������� �����");
        } else {
            addProgressStep("�� ������� �������� ������ ��� ������ �� �������");
            addProgressStep("��������� ������������ username � ����������� �������");
            showErrorMessage("�� ������� �������� ������ ��� ������ �� �������");
        }
    }

    private void handleFetchError(Exception ex) {
        addProgressStep("��������� ������: " + ex.getMessage());
        addProgressStep("���������� ��� ��� ��� ��������� ���������� � ����������");
        showErrorMessage("������ ��� ��������� ������: " + ex.getMessage());
        logger.error("Error fetching solved problems", ex);
    }

    private void handleLoadProblemsResult(Map<String, ProblemInfo> problems) {
        if (problems != null && !problems.isEmpty()) {
            allProblemsInfo = problems;
            problemsLoaded = true;

            addProgressStep("������� ��������� " + problems.size() + " �����");
            addProgressStep("���������� �� ��������� ������ ��������!");
            addProgressStep("���������� �������� ������� ���������...");

            // ������������� �������� ��� ������ ��������� ����� ��������
            easyCheckBox.setSelected(true);
            mediumCheckBox.setSelected(true);
            hardCheckBox.setSelected(true);

            updateUIState();
            updateStatistics();
            showInfoMessage("��������� " + problems.size() + " �����. ���������� �� ��������� �������!");
        } else {
            addProgressStep("�� ������� ��������� ���������� � �������");
            showErrorMessage("�� ������� ��������� ���������� � �������");
        }
    }

    private void handleLoadProblemsError(Exception ex) {
        addProgressStep("������ ��� �������� �����: " + ex.getMessage());
        addProgressStep("���������� ��� ��� ��� ��������� ���������� � ����������");
        showErrorMessage("������ ��� �������� �����: " + ex.getMessage());
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
            statisticsLabel.setText("����������: ��������� ������ ��� ��������� ����������");
            return;
        }

        try {
            int min = parseInteger(minField.getText().trim(), "����������� ��������");
            int max = parseInteger(maxField.getText().trim(), "������������ ��������");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            Set<ProblemDifficulty> selectedDifficulties = getSelectedDifficulties();

            if (selectedDifficulties.isEmpty()) {
                statisticsLabel.setText("����������: �������� ���� �� ���� ������� ���������");
                statisticsLabel.setForeground(Color.RED);
                return;
            }

            ProblemStatistics stats = randomService.getProblemStatistics(min, max, excludeSet, selectedDifficulties, allProblemsInfo);

            String statsText = String.format("�������� �����: %d (Easy: %d, Medium: %d, Hard: %d)",
                    stats.getTotal(), stats.getEasy(), stats.getMedium(), stats.getHard());
            statisticsLabel.setText(statsText);
            statisticsLabel.setForeground(new Color(0, 100, 0)); // �����-�������

        } catch (Exception e) {
            statisticsLabel.setText("����������: ������ � ����������");
            statisticsLabel.setForeground(Color.RED);
        }
    }

    /**
     * ������������ ����� ��� ����������� ���������� � ������
     */
    private void showProblemInfo(Integer problemNumber) {
        if (problemNumber == null || allProblemsInfo.isEmpty()) {
            return;
        }

        // ������� ���������� � ������ �� ������
        Optional<ProblemInfo> problemInfo = allProblemsInfo.values().stream()
                .filter(p -> Objects.equals(p.getNumber(), problemNumber))
                .findFirst();

        if (problemInfo.isPresent()) {
            ProblemInfo info = problemInfo.get();
            addProgressStep(String.format("������: #%d - %s [%s]",
                    info.getNumber(), info.getTitle(), info.getDifficulty()));
        } else {
            addProgressStep("���������� � ������ #" + problemNumber + " �� �������");
        }
    }

    // ������ ��� ������ � ����������
    private void clearProgress() {
        progressArea.setText("");
    }

    private void addProgressStep(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMessage = String.format("[%s] %s%n", timestamp, message);
            progressArea.append(formattedMessage);

            // �������������� ��������� � ���������� ���������
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

        // ��������� �������� �� ����� ��������
        if (loading) {
            easyCheckBox.setEnabled(false);
            mediumCheckBox.setEnabled(false);
            hardCheckBox.setEnabled(false);
        } else {
            updateUIState(); // ��������������� ��������� ����� ��������
        }
    }

    private int parseInteger(String value, String fieldName) throws NumberFormatException {
        if (value.isEmpty()) {
            throw new NumberFormatException("���� '" + fieldName + "' �� ����� ���� ������");
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("������������ �������� � ���� '" + fieldName + "'");
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "������", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "����������", JOptionPane.INFORMATION_MESSAGE);
    }
}