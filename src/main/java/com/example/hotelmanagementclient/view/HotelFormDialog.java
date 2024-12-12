package com.example.hotelmanagementclient.view;

import com.example.hotelmanagementclient.model.Hotel;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class HotelFormDialog extends JDialog {
    private JTextField nameField;
    private JTextField priceField;
    private JTextField availableRoomsField;
    private JComboBox<Integer> starsComboBox;
    private JTextField emailField;
    private JTextField phoneField;
    private JButton saveButton;
    private JButton cancelButton;

    @Getter
    private boolean succeeded;
    @Getter
    private Hotel hotel;

    public HotelFormDialog(Frame parent, String title, Hotel hotel) {
        super(parent, title, true);
        this.hotel = hotel != null ? hotel : new Hotel();

        initializeComponents();
        populateFields();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel nameLabel = new JLabel("Название:");
        nameField = new JTextField(20);

        JLabel priceLabel = new JLabel("Стоимость за ночь:");
        priceField = new JTextField(20);

        JLabel availableRoomsLabel = new JLabel("Свободные номера:");
        availableRoomsField = new JTextField(20);

        JLabel starsLabel = new JLabel("Звезды:");
        starsComboBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel phoneLabel = new JLabel("Телефон:");
        phoneField = new JTextField(20);

        saveButton = new JButton("Сохранить");
        cancelButton = new JButton("Отмена");

        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(priceLabel, gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(availableRoomsLabel, gbc);
        gbc.gridx = 1;
        panel.add(availableRoomsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(starsLabel, gbc);
        gbc.gridx = 1;
        panel.add(starsComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(emailLabel, gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        add(panel);

        // Назначение слушателей событий
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> onCancel());

        // Улучшение визуального оформления кнопок
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
    }

    private void populateFields() {
        if (hotel != null) {
            nameField.setText(hotel.getName());
            priceField.setText(hotel.getPricePerNight() != null ? hotel.getPricePerNight().toString() : "");
            availableRoomsField.setText(hotel.getAvailableRooms() != null ? hotel.getAvailableRooms().toString() : "");
            starsComboBox.setSelectedItem(hotel.getStars() != null ? hotel.getStars() : 1);
            emailField.setText(hotel.getEmail());
            phoneField.setText(hotel.getPhone());
        }
    }

    private void onSave() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();
        String availableRoomsText = availableRoomsField.getText().trim();
        Integer stars = (Integer) starsComboBox.getSelectedItem();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || availableRoomsText.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Double price;
        Integer availableRooms;
        try {
            price = Double.parseDouble(priceText);
            availableRooms = Integer.parseInt(availableRoomsText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные числовые значения.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        hotel.setName(name);
        hotel.setPricePerNight(price);
        hotel.setAvailableRooms(availableRooms);
        hotel.setStars(stars);
        hotel.setEmail(email);
        hotel.setPhone(phone);

        succeeded = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

}
