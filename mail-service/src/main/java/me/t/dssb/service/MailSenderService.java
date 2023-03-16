package me.t.dssb.service;

import me.t.dssb.dto.MailParams;

public interface MailSenderService {
    void send(MailParams mailParams);
}
