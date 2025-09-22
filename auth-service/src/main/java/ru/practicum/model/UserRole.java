package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
public class UserRole implements GrantedAuthority {

    private String name;

    @Override
    public String getAuthority() {
        return name;
    }
}
