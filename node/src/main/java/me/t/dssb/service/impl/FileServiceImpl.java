package me.t.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.t.dssb.dao.BinaryContentDAO;
import me.t.dssb.dao.DocumentAppDAO;
import me.t.dssb.dao.PhotoAppDAO;
import me.t.dssb.entity.BinaryContent;
import me.t.dssb.entity.DocumentApp;
import me.t.dssb.entity.PhotoApp;
import me.t.dssb.exception.UploadFileException;
import me.t.dssb.service.FileService;
import me.t.dssb.service.enums.LinkType;
import me.t.dssb.utils.CryptoTool;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final DocumentAppDAO documentAppDAO;
    private final PhotoAppDAO photoAppDAO;
    private final BinaryContentDAO binaryContentDAO;
    private final CryptoTool cryptoTool;

    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${link.address}")
    private String linkAddress;

    public FileServiceImpl(DocumentAppDAO documentAppDAO, PhotoAppDAO photoAppDAO,
                           BinaryContentDAO binaryContentDAO, CryptoTool cryptoTool) {
        this.documentAppDAO = documentAppDAO;
        this.photoAppDAO = photoAppDAO;
        this.binaryContentDAO = binaryContentDAO;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public DocumentApp processDoc(Message telegramMessage) {
        Document telegramDoc = telegramMessage.getDocument();
        String fileId = telegramDoc.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            DocumentApp transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return documentAppDAO.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    @Override
    public PhotoApp processPhoto(Message telegramMessage) {
        var photoSizeCount = telegramMessage.getPhoto().size();
        var photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        String fileId = telegramPhoto.getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            PhotoApp transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return photoAppDAO.save(transientAppPhoto);
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        byte[] fileInByte = downloadFile(filePath);
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArraysOfBytes(fileInByte)
                .build();
        return binaryContentDAO.save(transientBinaryContent);
    }

    private String getFilePath(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
    }

    private DocumentApp buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return DocumentApp.builder()
                .telegramFileId(telegramDoc.getFileId())
                .name(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .format(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private PhotoApp buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return PhotoApp.builder()
                .telegramFileId(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
                .build();
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri
                .replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        //TODO подумать над оптимизацией
        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    @Override
    public String generateLink(Long docId, LinkType linkType) {
        var hash = cryptoTool.hashOf(docId);
        return "http://" + linkAddress + "/" + linkType + "?id=" + hash;
    }
}
