package me.t.dssb.dao;

import me.t.dssb.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAppDAO extends JpaRepository<UserApp, Long> {
    Optional<UserApp> findUserAppByTelegramUID(Long telegramUID);

    Optional<UserApp> findUserAppById(Long id);

    Optional<UserApp> findUserAppByEmail(String email);
}
