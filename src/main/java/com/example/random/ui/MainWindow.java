package com.example.random.ui;

import com.example.random.service.LeetCodeService;
import com.example.random.service.RandomGeneratorService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

/**
 * ������� ���� ����������
 */
public class MainWindow {
    private static final String DEFAULT_USERNAME = "ArtProIT";
    private static final String FETCH_BUTTON_DEFAULT_TEXT = "�������� �������� ������ � LeetCode";
    private static final String FETCH_BUTTON_LOADING_TEXT = "��������� ������...";

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
        frame = new JFrame("��������� ���������� ����� LeetCode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
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
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("���������"));

        // ������ ����������
        profileField = new JTextField(DEFAULT_USERNAME);
        minField = new JTextField();
        maxField = new JTextField();
        excludeField = new JTextField();
        resultLabel = new JLabel("���������: ", SwingConstants.CENTER);

        // �������� ����������
        panel.add(new JLabel("������� LeetCode (username):"));
        panel.add(profileField);
        panel.add(new JLabel("����������� ��������:"));
        panel.add(minField);
        panel.add(new JLabel("������������ ��������:"));
        panel.add(maxField);
        panel.add(new JLabel("��������� (����� �������):"));
        panel.add(excludeField);
        panel.add(new JLabel(""));
        panel.add(resultLabel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        fetchButton = new JButton(FETCH_BUTTON_DEFAULT_TEXT);
        generateButton = new JButton("�������������");

        fetchButton.setPreferredSize(new Dimension(250, 30));
        generateButton.setPreferredSize(new Dimension(150, 30));

        panel.add(fetchButton);
        panel.add(generateButton);

        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("��� ����������"));

        progressArea = new JTextArea(10, 50);
        progressArea.setEditable(false);
        progressArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        progressArea.setBackground(new Color(248, 248, 248));
        progressArea.setText("����� � ������...\n");

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
            showErrorMessage("������� username ������� LeetCode");
            return;
        }

        clearProgress();
        addProgressStep("�������� ��������� ������ � LeetCode...");
        setFetchButtonLoading(true);

        SwingWorker<Set<Integer>, Void> worker = new SwingWorker<Set<Integer>, Void>() {
            @Override
            protected Set<Integer> doInBackground() throws Exception {
                // ��������� ����� � callback ��� ��������� ���������, ����������
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
        addProgressStep("�������� ��������� ���������� �����...");

        try {
            addProgressStep("��������� ��������� ���������...");
            int min = parseInteger(minField.getText().trim(), "����������� ��������");
            int max = parseInteger(maxField.getText().trim(), "������������ ��������");

            if (min > max) {
                addProgressStep("������: ����������� �������� ������ �������������");
                showErrorMessage("����������� �������� �� ����� ���� ������ �������������");
                return;
            }

            addProgressStep("������ ������ ����������...");
            Set<Integer> excludeSet = randomService.parseExcludeNumbers(excludeField.getText());
            addProgressStep("������� ����������: " + excludeSet.size());

            addProgressStep("������� ������ ��������� ����� � ��������� [" + min + ", " + max + "]...");
            Integer result = randomService.generateRandomNumber(min, max, excludeSet);

            if (result == null) {
                addProgressStep("��� ��������� ����� ��� ���������");
                resultLabel.setText("��� ��������� �����.");
            } else {
                addProgressStep("������� ������������� �����: " + result);
                resultLabel.setText("���������: " + result);
            }

        } catch (NumberFormatException ex) {
            addProgressStep("������ �����: " + ex.getMessage());
            resultLabel.setText("������ �����: " + ex.getMessage());
        } catch (Exception ex) {
            addProgressStep("����������� ������: " + ex.getMessage());
            resultLabel.setText("������: " + ex.getMessage());
        }
    }

    private void handleFetchResult(Set<Integer> solvedProblems) {
        if (solvedProblems != null && !solvedProblems.isEmpty()) {
            addProgressStep("������� �������� " + solvedProblems.size() + " �������� �����");
            addProgressStep("��������� ���� ����������...");

            String excludeText = randomService.formatExcludeNumbers(solvedProblems);
            excludeField.setText(excludeText);

            addProgressStep("������! ������ ��������� � ������ � �������������");
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
        ex.printStackTrace();
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