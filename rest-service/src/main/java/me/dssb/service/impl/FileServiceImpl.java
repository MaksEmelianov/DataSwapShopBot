package me.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.dssb.service.FileService;
import me.t.dssb.dao.DocumentAppDAO;
import me.t.dssb.dao.PhotoAppDAO;
import me.t.dssb.entity.DocumentApp;
import me.t.dssb.entity.PhotoApp;
import me.t.dssb.utils.CryptoTool;
import org.springframework.stereotype.Service;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final DocumentAppDAO documentAppDAO;
    private final PhotoAppDAO photoAppDAO;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(DocumentAppDAO documentAppDAO, PhotoAppDAO photoAppDAO, CryptoTool cryptoTool) {
        this.documentAppDAO = documentAppDAO;
        this.photoAppDAO = photoAppDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public DocumentApp getDocument(String hash) {
        var id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return documentAppDAO.findById(id).orElse(null);
    }

    @Override
    public PhotoApp getPhoto(String hash) {
        var id = cryptoTool.idOf(hash);
        if (id == null) {
            return null;
        }
        return photoAppDAO.findById(id).orElse(null);
    }
}
