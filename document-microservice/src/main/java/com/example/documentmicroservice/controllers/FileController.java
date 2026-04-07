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

import java.util.List;
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
            @RequestParam("projectId") UUID projectId,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "username", required = false) String username) {

        if (file == null || file.isEmpty()) {
            System.out.println("empty file");
        }

        try {
            Document document = documentService.saveDocument(file, projectId, projectName, userId);
            Version version = versionService.saveVersion(userId, document);
            fileService.saveFile(version.getId(), file);

        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
        }

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + username;    }

    @PostMapping("/uploadVersion")
    public String uploadVersions(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId,
            @RequestParam("docId") UUID documentId,
            @RequestParam(value = "name", required = false) String name) {

        if (file == null || file.isEmpty()) {
            System.out.println("empty file");
        }

        try {
            Document doc = documentService.findById(documentId);
            Version version = versionService.saveVersion(userId, doc);
            fileService.saveFile(version.getId(), file);

        } catch (Exception e) {
            System.err.println("error: " + e.getMessage());
            return "redirect:/error";
        }

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + name;
    }
}