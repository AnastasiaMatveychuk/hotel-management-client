package com.example.hotelmanagementclient;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.example.hotelmanagementclient.view.MainFrame;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Установка FlatLaf для современного дизайна
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Создание и отображение основного окна
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
