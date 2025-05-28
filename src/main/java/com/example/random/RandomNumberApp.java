package com.example.random;

import com.example.random.service.LeetCodeService;
import com.example.random.service.RandomGeneratorService;
import com.example.random.ui.MainWindow;

import javax.swing.SwingUtilities;

public class RandomNumberApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RandomNumberApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {

        RandomGeneratorService randomService = new RandomGeneratorService();
        LeetCodeService leetCodeService = new LeetCodeService();


        MainWindow mainWindow = new MainWindow(randomService, leetCodeService);
        mainWindow.show();
    }
}