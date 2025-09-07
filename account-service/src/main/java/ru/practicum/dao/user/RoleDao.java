package ru.practicum.dao.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * DAO роли
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("roles")
public class RoleDao {

    /**
     * Идентификатор роли
     */
    @Id
    @Column("role_uuid")
    private UUID roleUuid;

    /**
     * Название роли
     */
    @Column("name")
    private String name;

    /**
     * Описание роли
     */
    @Column("description")
    private String description;
}