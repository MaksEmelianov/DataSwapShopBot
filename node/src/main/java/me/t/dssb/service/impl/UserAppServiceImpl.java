package me.t.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.t.dssb.dao.UserAppDAO;
import me.t.dssb.dto.MailParams;
import me.t.dssb.entity.UserApp;
import me.t.dssb.service.UserAppService;
import me.t.dssb.utils.CryptoTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static me.t.dssb.entity.enums.UserState.BASIC_STATE;
import static me.t.dssb.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Log4j
@Service
public class UserAppServiceImpl implements UserAppService {
    private final UserAppDAO userAppDAO;
    private final CryptoTool cryptoTool;

    @Value("${service.mail.uri}")
    private String mailServiceUri;

    public UserAppServiceImpl(UserAppDAO userAppDAO, CryptoTool cryptoTool) {
        this.userAppDAO = userAppDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(UserApp userApp) {
        if (userApp.getIsActive()) {
            return "Вы уже зарегистрированы!";
        } else if (userApp.getEmail() != null) {
            return "Вам на почту уже было отправлено письмо. "
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        }
        userApp.setState(WAIT_FOR_EMAIL_STATE);
        userAppDAO.save(userApp);
        return "Введите, пожалуйста, ваш email:";
    }

    @Override
    public String setEmail(UserApp userApp, String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException e) {
            return "Введите, пожалуйста, корректный email. Для отмены команды введите /cancel";
        }
        var optional = userAppDAO.findUserAppByEmail(email);
        if (optional.isEmpty()) {
            userApp.setEmail(email);
            userApp.setState(BASIC_STATE);
            userApp = userAppDAO.save(userApp);

            var cryptoUserId = cryptoTool.hashOf(userApp.getId());
            var response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                var msg = String.format("Отправка эл. письма на почту %s не удалась.", email);
                log.error(msg);
                userApp.setEmail(null);
                userAppDAO.save(userApp);
                return msg;
            }
            return "Вам на почту было отправлено письмо."
                    + "Перейдите по ссылке в письме для подтверждения регистрации.";
        } else {
            return "Этот email уже используется. Введите корректный email."
                    + " Для отмены команды введите /cancel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        var request = new HttpEntity<>(mailParams, headers);
        return restTemplate.exchange(mailServiceUri,
                HttpMethod.POST,
                request,
                String.class);
    }
}
