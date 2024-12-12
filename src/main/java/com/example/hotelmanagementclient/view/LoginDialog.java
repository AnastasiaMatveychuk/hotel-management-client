package com.example.hotelmanagementclient.view;

import com.example.hotelmanagementclient.controller.MainController;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JButton registerButton;

    @Getter
    private boolean succeeded;
    @Getter
    private String username;
    @Getter
    private String password;

    private final MainController controller; // Добавляем поле для контроллера

    public LoginDialog(Frame parent, MainController controller) {
        super(parent, "Вход", true);
        this.controller = controller; // Инициализируем контроллер
        initializeComponents();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onCancel();
                System.exit(0);
            }
        });

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

        loginButton = new JButton("Вход");
        cancelButton = new JButton("Отмена");
        registerButton = new JButton("Регистрация");

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
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        // Назначение слушателей событий
        loginButton.addActionListener(e -> onLogin());
        cancelButton.addActionListener(e -> onCancel());
        registerButton.addActionListener(e -> onRegister());

        // Улучшение визуального оформления кнопок
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(33, 150, 243));
        registerButton.setForeground(Color.WHITE);
    }

    private void onLogin() {
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

    private void onRegister() {
        RegistrationDialog registrationDialog = new RegistrationDialog((Frame) getParent(), controller);
        registrationDialog.setVisible(true);
        if (registrationDialog.isSucceeded()) {
            // Получение данных регистрации
            String newUsername = registrationDialog.getUsername();
            String newPassword = registrationDialog.getPassword();
            // Передача данных в контроллер для регистрации с ролью USER
            controller.registerUser(newUsername, newPassword);
        }
    }
}
