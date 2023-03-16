package me.t.dssb.service;

import me.t.dssb.entity.DocumentApp;
import me.t.dssb.entity.PhotoApp;
import me.t.dssb.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    DocumentApp processDoc(Message telegramMessage);

    PhotoApp processPhoto(Message telegramMessage);

    String generateLink(Long docId, LinkType linkType);
}
