package com.example.documentmicroservice.controllers;

import com.example.documentmicroservice.models.Document;
import com.example.documentmicroservice.models.Version;
import com.example.documentmicroservice.services.DocumentService;
import com.example.documentmicroservice.services.FileService;
import com.example.documentmicroservice.services.VersionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Controller
public class FileController {
    private final DocumentService documentService;
    private final VersionService versionService;
    private final FileService fileService;

    public FileController(DocumentService documentService, VersionService versionService, FileService fileService) {
        this.documentService = documentService;
        this.versionService = versionService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String handleUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "projectId", required = false) UUID projectId, // Вече не е задължителен, ако имаме documentId
            @RequestParam(value = "documentId", required = false) UUID documentId, // ДОБАВЕНО
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "name", required = false, defaultValue = "User") String name) {

        Document document;
        if (documentId != null) {
            document = documentService.findById(documentId);
        } else {
            document = documentService.saveDocument(file, projectId, projectName, userId);
        }

        Version version = versionService.saveVersion(userId, document);
        fileService.saveFile(version.getId(), file);

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + name;
    }
}