package com.example.registration.dto;

public class RegisterResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String message;

    public RegisterResponse(Long id, String name, String email, String phone, String message) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMessage() { return message; }
}
