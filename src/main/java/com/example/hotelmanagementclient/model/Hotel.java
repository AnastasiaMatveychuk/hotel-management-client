package com.example.hotelmanagementclient.model;

public class Hotel {
    private Long id;
    private String name;
    private Double pricePerNight;
    private Integer availableRooms;
    private Integer stars;
    private String email;
    private String phone;

    // Конструкторы
    public Hotel() {}

    public Hotel(Long id, String name, Double pricePerNight, Integer availableRooms, Integer stars, String email, String phone) {
        this.id = id;
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.availableRooms = availableRooms;
        this.stars = stars;
        this.email = email;
        this.phone = phone;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public Integer getAvailableRooms() {
        return availableRooms;
    }

    public Integer getStars() {
        return stars;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void setAvailableRooms(Integer availableRooms) {
        this.availableRooms = availableRooms;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Метод для парсинга contactInfo
    public void parseContactInfo(String contactInfo) {
        if (contactInfo != null && !contactInfo.isEmpty()) {
            String[] parts = contactInfo.split(",");
            for (String part : parts) {
                part = part.trim();
                if (part.contains("@")) {
                    this.email = part;
                } else if (part.matches("\\+?\\d+")) {
                    this.phone = part;
                }
            }
        }
    }

    // Метод для объединения email и phone в contactInfo
    public String getContactInfo() {
        StringBuilder sb = new StringBuilder();
        if (email != null && !email.isEmpty()) {
            sb.append(email);
        }
        if (phone != null && !phone.isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(phone);
        }
        return sb.toString();
    }

    public void setContactInfo(String contactInfo) {
        parseContactInfo(contactInfo);
    }
}
