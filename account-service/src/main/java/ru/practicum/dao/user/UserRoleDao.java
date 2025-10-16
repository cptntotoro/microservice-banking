package ru.practicum.dao.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * DAO роли пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles")
public class UserRoleDao {
    /**
     * Идентификатор пользователя
     */
    @Column("user_uuid")
    private UUID userUuid;

    /**
     * Идентификатор роли
     */
    @Column("role_uuid")
    private UUID roleUuid;
}