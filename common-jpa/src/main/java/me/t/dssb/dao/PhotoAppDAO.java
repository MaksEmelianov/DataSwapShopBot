package me.t.dssb.dao;

import me.t.dssb.entity.PhotoApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoAppDAO extends JpaRepository<PhotoApp, Long> {
}
