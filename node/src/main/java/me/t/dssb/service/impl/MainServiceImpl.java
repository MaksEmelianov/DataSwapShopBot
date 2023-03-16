package me.t.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.t.dssb.dao.RawDataDAO;
import me.t.dssb.dao.UserAppDAO;
import me.t.dssb.entity.DocumentApp;
import me.t.dssb.entity.PhotoApp;
import me.t.dssb.entity.RawData;
import me.t.dssb.entity.UserApp;
import me.t.dssb.exception.UploadFileException;
import me.t.dssb.service.FileService;
import me.t.dssb.service.MainService;
import me.t.dssb.service.ProducerService;
import me.t.dssb.service.UserAppService;
import me.t.dssb.service.enums.LinkType;
import me.t.dssb.service.enums.ServiceCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static me.t.dssb.entity.enums.UserState.BASIC_STATE;
import static me.t.dssb.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static me.t.dssb.service.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final UserAppDAO userAppDAO;
    private final FileService fileService;
    private final UserAppService userAppService;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProducerService producerService, UserAppDAO userAppDAO,
                           FileService fileService, UserAppService userAppService) {
        this.rawDataDAO = rawDataDAO;
        this.producerService = producerService;
        this.userAppDAO = userAppDAO;
        this.fileService = fileService;
        this.userAppService = userAppService;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = userAppService.setEmail(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cancel и попробуйте снова!";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            DocumentApp doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            var answer = "Документ успешно загружен! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {
            PhotoApp photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_PHOTO);
            var answer = "Фото успешно загружено! "
                    + "Ссылка для скачивания: " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowToSendContent(Long chatId, UserApp userApp) {
        var userState = userApp.getState();
        if (!userApp.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте "
                    + "свою учетную запись для загрузки контента.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью /cancel для отправки файлов.";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(UserApp userApp, String cmd) {
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            return userAppService.registerUser(userApp);
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя.";
    }

    private String cancelProcess(UserApp userApp) {
        userApp.setState(BASIC_STATE);
        userAppDAO.save(userApp);
        return "Команда отменена!";
    }

    private UserApp findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        var optional = userAppDAO.findUserAppByTelegramUID(telegramUser.getId());
        if (optional.isEmpty()) {
            UserApp transientAppUser = UserApp.builder()
                    .telegramUID(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .state(BASIC_STATE)
                    .build();
            return userAppDAO.save(transientAppUser);
        }
        return optional.get();
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
