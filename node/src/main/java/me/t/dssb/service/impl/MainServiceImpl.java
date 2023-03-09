package me.t.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.t.dssb.dao.RawDataDAO;
import me.t.dssb.dao.UserAppDAO;
import me.t.dssb.entity.RawData;
import me.t.dssb.entity.UserApp;
import me.t.dssb.service.MainService;
import me.t.dssb.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static me.t.dssb.entity.enums.UserState.BASIC_STATE;
import static me.t.dssb.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static me.t.dssb.service.enums.ServiceCommands.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final UserAppDAO userAppDAO;

    @Autowired
    public MainServiceImpl(RawDataDAO rawDataDAO,
                           ProducerService producerService, UserAppDAO userAppDAO) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.userAppDAO = userAppDAO;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var userApp = findOrSaveUserApp(update);
        var userState = userApp.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            output = cancelProcess(userApp);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(userApp, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            // TODO: 09.03.2023 добавить обработку емаила
        } else {
            log.error("Unknown user state" + userState);
            output = "Unknown error! Enter /cancel and try again";
        }

        var chatId = update.getMessage().getChatId();
        sentAnswer(output, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var chatId = update.getMessage().getChatId();
        var userApp = findOrSaveUserApp(update);
        if (isNotAllowToSentContent(chatId, userApp)) {
            return;
        }

        // TODO: 09.03.2023 добавить сохранение фото
        var answer = "Photo successfully loaded! Link for down: https://ya.ru";
        sentAnswer(answer, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var chatId = update.getMessage().getChatId();
        var userApp = findOrSaveUserApp(update);
        if (isNotAllowToSentContent(chatId, userApp)) {
            return;
        }

        // TODO: 09.03.2023 добавить сохранение документа
        var answer = "Doc successfully loaded! Link for down: https://ya.ru";
        sentAnswer(answer, chatId);
    }

    private boolean isNotAllowToSentContent(Long chatId, UserApp userApp) {
        var userState = userApp.getState();
        if (!userApp.getIsActive()) {
            var error = "User is not active. Register or confirm for load content";
            sentAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Canceled current command with the help command /cancel";
            sentAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sentAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(UserApp userApp, String text) {
        if (REGISTRATION.equals(text)) {
            // TODO: 09.03.2023 добавить регистрацию
            return "Temporarily unavailable";
        } else if (HELP.equals(text)) {
            return help();
        } else if (START.equals(text)) {
            return "Enter /help to view list of commands";
        } else {
            return "Unknown command. Enter /help to view list of commands";
        }
    }

    private String help() {
        return "List of commands:\n"
                + "/help - HELP;\n"
                + "/cancel - Cancel command;\n"
                + "/registration - Register user.";
    }

    private String cancelProcess(UserApp userApp) {
        userApp.setState(BASIC_STATE);
        userAppDAO.save(userApp);
        return "Command cancelled";
    }

    public UserApp findOrSaveUserApp(Update update) {
        User telegramUser = update.getMessage().getFrom();
        UserApp persistUser = userAppDAO.findUserAppByTelegramUID(telegramUser.getId());
        if (Objects.isNull(persistUser)) {
            UserApp transientUser = UserApp.builder()
                    .telegramUID(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    // TODO: 07.03.2023 изменить после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return userAppDAO.save(transientUser);
        }
        return persistUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
