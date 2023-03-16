package me.t.dssb.dao;

import me.t.dssb.entity.DocumentApp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAppDAO extends JpaRepository<DocumentApp, Long> {
}
