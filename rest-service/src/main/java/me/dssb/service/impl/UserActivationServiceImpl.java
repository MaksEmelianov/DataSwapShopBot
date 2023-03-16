package me.dssb.service.impl;

import me.dssb.service.UserActivationService;
import me.t.dssb.dao.UserAppDAO;
import me.t.dssb.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final UserAppDAO userAppDAO;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(UserAppDAO userAppDAO, CryptoTool cryptoTool) {
        this.userAppDAO = userAppDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public boolean activation(String cryptoUserId) {
        var userId = cryptoTool.idOf(cryptoUserId);
        var optional = userAppDAO.findById(userId);
        if (optional.isPresent()) {
            var user = optional.get();
            user.setIsActive(true);
            userAppDAO.save(user);
            return true;
        }
        return false;
    }
}
