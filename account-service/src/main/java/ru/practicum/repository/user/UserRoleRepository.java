package ru.practicum.repository.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserRoleDao;

import java.util.UUID;

/**
 * Репозиторий ролей пользователей
 */
@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRoleDao, UUID> {

    /**
     * Получить роли пользователя по его идентификатору
     *
     * @param userUuid Идентификатор пользователя
     * @return Список ролей
     */
    @Query("SELECT r.name FROM roles r JOIN user_roles ur ON r.role_uuid = ur.role_uuid WHERE ur.user_uuid = :userUuid")
    Flux<String> findRoleNamesByUserUuid(UUID userUuid);

//    /**
//     * Добавить роль пользователю
//     *
//     * @param userUuid Идентификатор пользователя
//     * @param roleUuid Идентификатор роли
//     */
//    @Query("INSERT INTO user_roles (user_uuid, role_uuid) " +
//            "VALUES (:userUuid, (SELECT role_uuid FROM roles WHERE name = :roleName))")
//    Mono<Void> addRoleToUser(UUID userUuid, UUID roleUuid);
//
//    /**
//     * Удалить роль у пользователя
//     *
//     * @param userUuid Идентификатор пользователя
//     * @param roleUuid Идентификатор роли
//     */
//    @Query("DELETE FROM user_roles WHERE user_uuid = :userUuid " +
//            "AND role_uuid = (SELECT role_uuid FROM roles WHERE name = :roleName)")
//    Mono<Void> removeRoleFromUser(UUID userUuid, UUID roleUuid);

    /**
     * Проверить, есть ли у пользователя роль
     *
     * @param userUuid Идентификатор пользователя
     * @param roleName Название роли
     */
    @Query("SELECT COUNT(*) > 0 FROM user_roles ur " +
            "JOIN roles r ON ur.role_uuid = r.role_uuid " +
            "WHERE ur.user_uuid = :userUuid AND r.name = :roleName")
    Mono<Boolean> userHasRole(UUID userUuid, String roleName);

    /**
     * Добавить роль пользователю по названию роли
     */
    @Query("INSERT INTO user_roles (user_uuid, role_uuid) " +
            "SELECT :userUuid, r.role_uuid FROM roles r WHERE r.name = :roleName")
    Mono<Void> addUserRole(UUID userUuid, String roleName);

    /**
     * Удалить роль у пользователя по названию роли
     */
    @Query("DELETE FROM user_roles WHERE user_uuid = :userUuid " +
            "AND role_uuid = (SELECT role_uuid FROM roles WHERE name = :roleName)")
    Mono<Void> removeUserRole(UUID userUuid, String roleName);
}