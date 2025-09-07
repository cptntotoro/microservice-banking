package ru.practicum.client;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Boolean emailVerified;
    private Boolean accountEnabled;
}
