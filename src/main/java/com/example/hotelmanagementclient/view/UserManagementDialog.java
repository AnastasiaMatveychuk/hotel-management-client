package com.example.hotelmanagementclient.view;

import com.example.hotelmanagementclient.controller.MainController;
import com.example.hotelmanagementclient.controller.MainController.UserDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class UserManagementDialog extends JDialog {
    private final MainController controller;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton assignAdminButton;
    private JButton removeAdminButton;

    public UserManagementDialog(Frame parent, MainController controller) {
        super(parent, "Управление пользователями", true);
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        setSize(600, 400);
        setLocationRelativeTo(getParent());

        String[] columnNames = {"ID", "Имя пользователя", "Роль"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Делать ячейки таблицы не редактируемыми
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);

        assignAdminButton = new JButton("Назначить администратором");
        removeAdminButton = new JButton("Снять права администратора");

        assignAdminButton.addActionListener(e -> assignAdminRole());
        removeAdminButton.addActionListener(e -> removeAdminRole());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(assignAdminButton);
        buttonPanel.add(removeAdminButton);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Метод для заполнения таблицы пользователей
    public void populateUserTable(List<UserDTO> users) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (UserDTO user : users) {
                Object[] row = {user.getId(), user.getUsername(), user.getRole()};
                tableModel.addRow(row);
            }
        });
    }

    private void assignAdminRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Предупреждение", "Пожалуйста, выберите пользователя.");
            return;
        }

        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 2);
        if (currentRole.equalsIgnoreCase("ADMIN")) {
            showInfo("Информация", "Выбранный пользователь уже является администратором.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите назначить этого пользователя администратором?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        controller.updateUserRole(userId, "ADMIN");
    }

    private void removeAdminRole() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Предупреждение", "Пожалуйста, выберите пользователя.");
            return;
        }

        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 2);
        if (!currentRole.equalsIgnoreCase("ADMIN")) {
            showInfo("Информация", "Выбранный пользователь не является администратором.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите снять права администратора у этого пользователя?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        controller.updateUserRole(userId, "USER");
    }

    private void showWarning(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE)
        );
    }

    private void showInfo(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE)
        );
    }
}
