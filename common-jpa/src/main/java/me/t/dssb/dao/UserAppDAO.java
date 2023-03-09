package me.t.dssb.dao;

import me.t.dssb.entity.UserApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAppDAO extends JpaRepository<UserApp, Long> {
    UserApp findUserAppByTelegramUID(Long telegramUID);
}
