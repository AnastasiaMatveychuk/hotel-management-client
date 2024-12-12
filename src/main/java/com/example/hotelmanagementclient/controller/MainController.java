package com.example.hotelmanagementclient.controller;

import com.example.hotelmanagementclient.model.Hotel;
import com.example.hotelmanagementclient.view.HotelFormDialog;
import com.example.hotelmanagementclient.view.LoginDialog;
import com.example.hotelmanagementclient.view.MainFrame;
import com.example.hotelmanagementclient.view.UserManagementDialog;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Base64;

public class MainController {
    private final MainFrame mainFrame;
    private final String BASE_URL = "http://localhost:8080/api/hotels";
    private final String AUTH_URL = "http://localhost:8080/api/auth";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private String username;
    private String password;
    private String role;

    // Счетчик неудачных попыток входа (опционально)
    private int loginAttempts = 0;
    private final int MAX_LOGIN_ATTEMPTS = 5;

    public MainController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        showLoginDialog();
    }

    private void showLoginDialog() {
        while (loginAttempts < MAX_LOGIN_ATTEMPTS) {
            LoginDialog loginDlg = new LoginDialog(mainFrame, this); // Передаём контроллер
            loginDlg.setVisible(true);
            if (loginDlg.isSucceeded()) {
                this.username = loginDlg.getUsername();
                this.password = loginDlg.getPassword();
                fetchUserRole();
                return; // Выход из метода после успешного входа
            } else {
                loginAttempts++;
                JOptionPane.showMessageDialog(mainFrame,
                        "Вход отменён пользователем.",
                        "Отмена",
                        JOptionPane.INFORMATION_MESSAGE);
                if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Превышено максимальное количество попыток входа.",
                            "Ошибка входа",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }

    // Метод для получения роли пользователя
    private void fetchUserRole() {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL + "/me"))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        handleUserRoleResponse(response.body());
                    } else if (response.statusCode() == 401) {
                        loginAttempts++;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                "Неверное имя пользователя или пароль.",
                                "Ошибка входа",
                                JOptionPane.ERROR_MESSAGE));
                        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(mainFrame,
                                        "Превышено максимальное количество попыток входа.",
                                        "Ошибка входа",
                                        JOptionPane.ERROR_MESSAGE);
                                System.exit(0);
                            });
                        } else {
                            SwingUtilities.invokeLater(this::showLoginDialog);
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(mainFrame,
                                    "Не удалось получить информацию о пользователе: " + response.body(),
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                            showLoginDialog();
                        });
                    }
                })
                .exceptionally(e -> {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(mainFrame,
                                "Не удалось получить информацию о пользователе.",
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                        showLoginDialog();
                    });
                    return null;
                });
    }

    private void handleUserRoleResponse(String responseBody) {
        try {
            // Парсинг ответа
            UserDTO userDTO = mapper.readValue(responseBody, UserDTO.class);
            this.role = userDTO.getRole();
            SwingUtilities.invokeLater(() -> mainFrame.updateUIBasedOnRole(role));
            loadHotels();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка", "Ошибка при обработке данных о пользователе.");
            SwingUtilities.invokeLater(this::showLoginDialog);
        }
    }


    // Загрузка всех гостиниц
    public void loadHotels() {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::populateTable)
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось загрузить данные о гостиницах.");
                    return null;
                });
    }

    // Заполнение таблицы данными
    private void populateTable(String responseBody) {
        try {
            List<Hotel> hotels = mapper.readValue(responseBody, new TypeReference<List<Hotel>>() {});
            SwingUtilities.invokeLater(() -> {
                mainFrame.clearTable();
                for (Hotel hotel : hotels) {
                    hotel.parseContactInfo(hotel.getContactInfo()); // Парсинг контактной информации
                    Object[] row = {
                            hotel.getId(),
                            hotel.getName(),
                            hotel.getPricePerNight(),
                            hotel.getAvailableRooms(),
                            hotel.getStars(),
                            hotel.getEmail(),
                            hotel.getPhone()
                    };
                    mainFrame.addHotelToTable(row);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка", "Ошибка при обработке данных.");
        }
    }

    // Поиск гостиниц по названию
    public void onSearch() {
        String query = mainFrame.getSearchText().trim();
        if (query.isEmpty()) {
            showWarning("Предупреждение", "Пожалуйста, введите название для поиска.");
            return;
        }

        // Кодирование параметра запроса
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = BASE_URL + "/search?name=" + encodedQuery;

        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        populateTable(response.body());
                        SwingUtilities.invokeLater(() -> mainFrame.returnButton.setVisible(true));
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                "Не удалось выполнить поиск: " + response.body(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE));
                    }
                })
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось выполнить поиск.");
                    return null;
                });
    }

    // Добавление новой гостиницы
    public void onAddHotel() {
        if (!isAdmin()) {
            showError("Доступ запрещён", "У вас нет прав для выполнения этого действия.");
            return;
        }

        HotelFormDialog dialog = new HotelFormDialog(mainFrame, "Добавить Гостиницу", null);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            Hotel newHotel = dialog.getHotel();
            try {
                String json = mapper.writeValueAsString(newHotel);
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL))
                        .header("Authorization", "Basic " + encodedAuth)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                SwingUtilities.invokeLater(this::loadHotels);
                                showInfo("Успех", "Гостиница добавлена успешно.");
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                        "Не удалось добавить гостиницу: " + response.body(),
                                        "Ошибка",
                                        JOptionPane.ERROR_MESSAGE));
                            }
                        })
                        .exceptionally(e -> {
                            showError("Ошибка", "Не удалось добавить гостиницу.");
                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Ошибка", "Не удалось сериализовать данные.");
            }
        }
    }

    // Редактирование выбранной гостиницы
    public void onEditHotel() {
        if (!isAdmin()) {
            showError("Доступ запрещён", "У вас нет прав для выполнения этого действия.");
            return;
        }

        int selectedRow = mainFrame.getHotelTable().getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Предупреждение", "Пожалуйста, выберите гостиницу для редактирования.");
            return;
        }

        Long id = (Long) mainFrame.getHotelTable().getValueAt(selectedRow, 0);
        String name = (String) mainFrame.getHotelTable().getValueAt(selectedRow, 1);
        Double price = (Double) mainFrame.getHotelTable().getValueAt(selectedRow, 2);
        Integer availableRooms = (Integer) mainFrame.getHotelTable().getValueAt(selectedRow, 3);
        Integer stars = (Integer) mainFrame.getHotelTable().getValueAt(selectedRow, 4);
        String email = (String) mainFrame.getHotelTable().getValueAt(selectedRow, 5);
        String phone = (String) mainFrame.getHotelTable().getValueAt(selectedRow, 6);

        Hotel existingHotel = new Hotel();
        existingHotel.setId(id);
        existingHotel.setName(name);
        existingHotel.setPricePerNight(price);
        existingHotel.setAvailableRooms(availableRooms);
        existingHotel.setStars(stars);
        existingHotel.setEmail(email);
        existingHotel.setPhone(phone);

        HotelFormDialog dialog = new HotelFormDialog(mainFrame, "Редактировать Гостиницу", existingHotel);
        dialog.setVisible(true);
        if (dialog.isSucceeded()) {
            Hotel updatedHotel = dialog.getHotel();
            try {
                String json = mapper.writeValueAsString(updatedHotel);
                String url = BASE_URL + "/" + id;
                String auth = username + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Basic " + encodedAuth)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                SwingUtilities.invokeLater(this::loadHotels);
                                showInfo("Успех", "Гостиница обновлена успешно.");
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(mainFrame,
                                            "Не удалось обновить гостиницу: " + response.body(),
                                            "Ошибка",
                                            JOptionPane.ERROR_MESSAGE);
                                });
                            }
                        })
                        .exceptionally(e -> {
                            showError("Ошибка", "Не удалось обновить гостиницу.");
                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Ошибка", "Не удалось сериализовать данные.");
            }
        }
    }

    // Удаление выбранной гостиницы
    public void onDeleteHotel() {
        if (!isAdmin()) {
            showError("Доступ запрещён", "У вас нет прав для выполнения этого действия.");
            return;
        }

        int selectedRow = mainFrame.getHotelTable().getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Предупреждение", "Пожалуйста, выберите гостиницу для удаления.");
            return;
        }

        Long id = (Long) mainFrame.getHotelTable().getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить эту гостиницу?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String url = BASE_URL + "/" + id;
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        SwingUtilities.invokeLater(this::loadHotels);
                        showInfo("Успех", "Гостиница удалена успешно.");
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                "Не удалось удалить гостиницу: " + response.body(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE));
                    }
                })
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось удалить гостиницу.");
                    return null;
                });
    }

    // Показ статистики
    public void onShowStatistics() {
        String url = BASE_URL + "/statistics/average-price";
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        Double averagePrice = mapper.readValue(responseBody, Double.class);
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame,
                                "Средняя стоимость за ночь: " + averagePrice,
                                "Статистика",
                                JOptionPane.INFORMATION_MESSAGE));
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Ошибка", "Ошибка при обработке статистических данных.");
                    }
                })
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось получить статистические данные.");
                    return null;
                });
    }

    // Показ информации об авторе
    public void onAbout() {
        JOptionPane.showMessageDialog(mainFrame,
                "Автор: Матвейчук Анастасия, Финансовый университет\nКонтакт: your.email@example.com",
                "Об авторе",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Метод для регистрации нового пользователя (роль USER по умолчанию)
    public void registerUser(String newUsername, String newPassword) {
        String url = AUTH_URL + "/register";

        // Создание JSON-объекта для регистрации с ролью USER
        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\", \"role\":\"%s\"}",
                newUsername, newPassword, "USER");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, "Регистрация прошла успешно. Вы можете войти в систему.", "Успех", JOptionPane.INFORMATION_MESSAGE));
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, "Не удалось зарегистрировать пользователя: " + response.body(), "Ошибка", JOptionPane.ERROR_MESSAGE));
                    }
                })
                .exceptionally(e -> {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(mainFrame, "Не удалось зарегистрировать пользователя.", "Ошибка", JOptionPane.ERROR_MESSAGE));
                    return null;
                });
    }

    // Метод для выхода из аккаунта
    public void onLogout() {
        // Очистка данных сессии
        this.username = null;
        this.password = null;
        this.role = null;
        loginAttempts = 0; // Сброс счетчика попыток входа

        // Очистка таблицы
        SwingUtilities.invokeLater(() -> {
            mainFrame.clearTable();
            mainFrame.updateUIBasedOnRole(""); // Скрыть все кнопки
            mainFrame.returnButton.setVisible(false); // Скрыть кнопку возврата
        });

        // Показать диалог входа
        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    // Метод для возврата к списку всех отелей
    public void onReturnAllHotels() {
        loadHotels();

        // Скрыть кнопку возврата после загрузки всех отелей
        SwingUtilities.invokeLater(() -> mainFrame.returnButton.setVisible(false));
    }

    // Метод для управления пользователями (открытие диалога)
    public void onManageUsers() {
        if (!isAdmin()) {
            showError("Доступ запрещён", "У вас нет прав для выполнения этого действия.");
            return;
        }
        fetchAllUsers();
    }

    // Метод для получения всех пользователей
    public void fetchAllUsers() {
        String url = AUTH_URL + "/users"; // Новый эндпоинт
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        handleFetchAllUsersResponse(response.body());
                    } else {
                        showError("Ошибка", "Не удалось загрузить список пользователей: " + response.body());
                    }
                })
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось загрузить список пользователей.");
                    return null;
                });
    }

    private void handleFetchAllUsersResponse(String responseBody) {
        try {
            List<UserDTO> users = mapper.readValue(responseBody, new TypeReference<List<UserDTO>>() {});
            // Открываем диалог управления пользователями
            SwingUtilities.invokeLater(() -> {
                UserManagementDialog dialog = new UserManagementDialog(mainFrame, this);
                dialog.populateUserTable(users);
                dialog.setVisible(true);
            });
        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка", "Ошибка при обработке данных пользователей.");
        }
    }

    // Метод для обновления роли пользователя
    public void updateUserRole(Long userId, String newRole) {
        String url = AUTH_URL + "/users/" + userId + "/role"; // Новый эндпоинт

        String json = String.format("{\"role\":\"%s\"}", newRole);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        showInfo("Успех", "Роль пользователя обновлена.");
                        fetchAllUsers(); // Обновляем список пользователей
                    } else {
                        showError("Ошибка", "Не удалось обновить роль пользователя: " + response.body());
                    }
                })
                .exceptionally(e -> {
                    showError("Ошибка", "Не удалось обновить роль пользователя.");
                    return null;
                });
    }

    // Вспомогательные методы для отображения сообщений
    private void showError(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.ERROR_MESSAGE)
        );
    }

    private void showInfo(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.INFORMATION_MESSAGE)
        );
    }

    private void showWarning(String title, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE)
        );
    }

    // Метод для проверки, является ли пользователь администратором
    private boolean isAdmin() {
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    // Внутренний класс DTO
    public static class UserDTO {
        private Long id; // Добавлено поле ID
        private String username;
        private String role;

        // Геттеры и сеттеры
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
