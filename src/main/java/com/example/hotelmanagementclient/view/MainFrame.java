package com.example.hotelmanagementclient.view;

import com.example.hotelmanagementclient.controller.MainController;
import lombok.Getter;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class MainFrame extends JFrame {
    private final MainController controller;

    // Компоненты интерфейса
    @Getter
    JTable hotelTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    JButton searchButton;
    JButton addButton;
    JButton editButton;
    JButton deleteButton;
    JButton statisticsButton;
    JButton aboutButton;
    JButton logoutButton;
    public JButton returnButton;
    private JButton manageUsersButton;

    private BackgroundPanel backgroundPanel;

    public MainFrame() {
        controller = new MainController(this);
        initialize();
    }

    private void initialize() {
        setTitle("Информационно-справочная система Гостиницы");
        setSize(1500, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Центрирование окна

        // Настройка шрифта для всех компонентов
        setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));

        // Установка панели с фоновым изображением
        backgroundPanel = new BackgroundPanel("/images/default_background.jpg");
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Создание панели поиска и кнопок
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setOpaque(false); // Прозрачность для видимости фона
        searchField = new JTextField(25);
        searchButton = new JButton("Поиск");
        addButton = new JButton("Добавить Гостиницу");
        statisticsButton = new JButton("Статистика");
        aboutButton = new JButton("Об авторе");
        logoutButton = new JButton("Выйти из аккаунта");
        returnButton = new JButton("Вернуться к списку всех отелей");
        manageUsersButton = new JButton("Управление пользователями");

        topPanel.add(new JLabel("Поиск по названию:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(addButton);
        topPanel.add(statisticsButton);
        topPanel.add(aboutButton);
        topPanel.add(manageUsersButton);
        topPanel.add(returnButton);
        topPanel.add(logoutButton);

        // Создание таблицы
        String[] columnNames = {"ID", "Название", "Стоимость за ночь", "Свободные номера", "Звезды", "Email", "Телефон"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hotelTable = new JTable(tableModel);
        hotelTable.setRowHeight(25);
        hotelTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        hotelTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hotelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScrollPane = new JScrollPane(hotelTable);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);

        // Создание панели действий
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        editButton = new JButton("Редактировать");
        deleteButton = new JButton("Удалить");
        bottomPanel.add(editButton);
        bottomPanel.add(deleteButton);

        // Добавление компонентов в основное окно
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Назначение слушателей событий
        searchButton.addActionListener(e -> controller.onSearch());
        addButton.addActionListener(e -> controller.onAddHotel());
        editButton.addActionListener(e -> controller.onEditHotel());
        deleteButton.addActionListener(e -> controller.onDeleteHotel());
        statisticsButton.addActionListener(e -> controller.onShowStatistics());
        aboutButton.addActionListener(e -> controller.onAbout());
        logoutButton.addActionListener(e -> controller.onLogout());
        returnButton.addActionListener(e -> controller.onReturnAllHotels());
        manageUsersButton.addActionListener(e -> controller.onManageUsers());

        // По умолчанию скрываем кнопки
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        returnButton.setVisible(false);
        logoutButton.setVisible(false);
        manageUsersButton.setVisible(false);
    }

    // Метод для установки шрифта по умолчанию для всех компонентов
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

    public void updateUIBasedOnRole(String role) {
        if (role.equalsIgnoreCase("ADMIN")) {
            // Устанавливаем фон администратора
            backgroundPanel.setBackgroundImage("/images/admin_background.jpg");
            addButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            manageUsersButton.setVisible(true);
            hotelTable.setVisible(true);
        } else if (role.equalsIgnoreCase("USER")) {
            // Устанавливаем фон пользователя
            backgroundPanel.setBackgroundImage("/images/user_background.jpg");
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
            manageUsersButton.setVisible(false);
            hotelTable.setVisible(true);
        }
        logoutButton.setVisible(true); // Кнопка видима для всех
    }

    // Методы для работы с таблицей
    public void addHotelToTable(Object[] rowData) {
        tableModel.addRow(rowData);
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public String getSearchText() {
        return searchField.getText();
    }

    // Внутренний класс для фонового изображения
    private static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String imagePath) {
            setBackgroundImage(imagePath);
        }

        public void setBackgroundImage(String imagePath) {
            try {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl == null) {
                    throw new IOException("Файл изображения не найден.");
                }
                BufferedImage img = ImageIO.read(imageUrl);
                backgroundImage = img;
                repaint();
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки фонового изображения: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                int width = getWidth();    // Ширина панели
                int height = getHeight();  // Высота панели

                // Рисуем изображение, растягивая его на весь размер панели
                g.drawImage(backgroundImage, 0, 0, width, height, this);
            }
        }

    }
}
