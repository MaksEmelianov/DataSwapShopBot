package me.dssb.service;

import me.t.dssb.entity.DocumentApp;
import me.t.dssb.entity.PhotoApp;

public interface FileService {
    DocumentApp getDocument(String id);
    PhotoApp getPhoto(String id);
}
