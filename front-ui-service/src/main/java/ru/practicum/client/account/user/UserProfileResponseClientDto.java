package ru.practicum.client.account.user;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfileResponseClientDto {
    /**
     * Идентификатор пользователя
     */
    private UUID id;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Email пользователя
     */
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Boolean emailVerified;
    private Boolean accountEnabled;
}
