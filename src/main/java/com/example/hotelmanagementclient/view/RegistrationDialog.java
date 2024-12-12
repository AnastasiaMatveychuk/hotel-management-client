package com.example.hotelmanagementclient.view;

import com.example.hotelmanagementclient.controller.MainController;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class RegistrationDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton cancelButton;

    @Getter
    private boolean succeeded;
    @Getter
    private String username;
    @Getter
    private String password;

    private final MainController controller;

    public RegistrationDialog(Frame parent, MainController controller) {
        super(parent, "Регистрация", true);
        this.controller = controller;
        initializeComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel usernameLabel = new JLabel("Имя пользователя:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordField = new JPasswordField(20);

        registerButton = new JButton("Регистрация");
        cancelButton = new JButton("Отмена");

        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        // Назначение слушателей событий
        registerButton.addActionListener(e -> onRegister());
        cancelButton.addActionListener(e -> onCancel());

        // Улучшение визуального оформления кнопок
        registerButton.setBackground(new Color(33, 150, 243));
        registerButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
    }

    private void onRegister() {
        username = usernameField.getText().trim();
        password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        succeeded = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
