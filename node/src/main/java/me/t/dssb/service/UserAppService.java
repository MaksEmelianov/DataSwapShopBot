package me.t.dssb.service;

import me.t.dssb.entity.UserApp;

public interface UserAppService {
    String registerUser(UserApp userApp);
    String setEmail(UserApp userApp, String email);
}
