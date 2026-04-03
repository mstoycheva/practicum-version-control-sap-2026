package com.example.documentmicroservice.controllers;

import com.example.documentmicroservice.models.Document;
import com.example.documentmicroservice.models.File;
import com.example.documentmicroservice.models.Version;
import com.example.documentmicroservice.services.DocumentService;
import com.example.documentmicroservice.services.FileService;
import com.example.documentmicroservice.services.VersionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class DocumentController {
    private final DocumentService documentService;
    private final FileService fileService;

    public DocumentController(DocumentService documentService, FileService fileService, VersionService versionService) {
        this.documentService = documentService;
        this.fileService = fileService;
    }

    @GetMapping("/documents")
    public String showWorkspace(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "name", required = false, defaultValue = "User") String name,
            Model model) {

        if (userId == null) return "redirect:/error";

        Map<String, Object> workspaceData = documentService.getWorkspaceData(userId);
        @SuppressWarnings("unchecked")
        Map<String, Object> groupedDocs = (Map<String, Object>) workspaceData.getOrDefault("groupedDocs", new java.util.HashMap<>());

        List<Map<String, Object>> allProjects;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/project-microservice/projects/user/" + userId;
            allProjects = restTemplate.getForObject(url, List.class);
            model.addAttribute("allProjects", allProjects);
            if (allProjects != null) {
                model.addAttribute("allProjects", allProjects);

                for (Map<String, Object> proj : allProjects) {
                    String projectName = (String) proj.get("name");
                    if (!groupedDocs.containsKey(projectName)) {
                        groupedDocs.put(projectName, new ArrayList<>());
                    }
                }
            } else {
                model.addAttribute("allProjects", new ArrayList<>());
            }
        } catch (Exception e) {
            System.err.println("Project fetch failed: " + e.getMessage());
        }
        String role = "reader";
        try {
            RestTemplate restTemplate = new RestTemplate();
            String authUrl = "http://localhost:8080/auth-microservice/users/" + userId;

            ResponseEntity<Map> response = restTemplate.getForEntity(authUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                role = response.getBody().get("role").toString();
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }

        model.addAttribute("userRole", role.toLowerCase());
        model.addAttribute("userId", userId);
        model.addAttribute("username", name);
        model.addAttribute("groupedDocs", groupedDocs);
        model.addAttribute("activeDocs", workspaceData.get("activeDocs"));
        model.addAttribute("draftDocs", workspaceData.get("draftDocs"));

        return "documents";
    }

    @GetMapping("/documents/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        File fileEntity = fileService.getFileEntityByDocumentId(id);
        Document doc = documentService.findById(id);
        byte[] data = fileEntity.getFile_path();
        String contentType = fileEntity.getContent();

        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @PostMapping("/documents")
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId,
            @RequestParam(value = "projectId", required = false) UUID projectId,
            @RequestParam(value = "documentId", required = false) UUID documentId,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "name", required = false, defaultValue = "User") String name) {

        Document document;
        if (documentId != null) {
            document = documentService.findById(documentId);
        } else {
            document = documentService.saveDocument(file, projectId, projectName, userId);
        }

        // Version version = versionService.saveVersion(userId, document);
        // fileService.saveFile(version.getId(), file);

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + name;    }

       private byte[] safeGetBytes(MultipartFile file) {
            throw new RuntimeException("Failed to read file bytes");
        }


    @PostMapping("/documents-draft")
    public String uploadDraft(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") UUID userId,
            @RequestParam("projectId") UUID projectId,
            @RequestParam(value = "name", required = false, defaultValue = "User") String name) {

        Document document = documentService.saveAsDraft(file, projectId, userId);
        // Version version = versionService.saveVersion(userId, document);
        // fileService.saveFile(version.getId(), file);

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + name;
    }


    @GetMapping("/documents/count")
    @ResponseBody
    public long countDocuments() {
        return documentService.countAllDocuments();
    }

    @GetMapping("/documents/all")
    @ResponseBody
    public List<Document> getAllDocuments() {
        return documentService.findAll();
    }

    @GetMapping("/projects/user/{userId}")
    @ResponseBody
    public List<Files> getFilesByUser(@PathVariable UUID userId) {
        //return fileService.getAllFiles(userId);
        return new ArrayList<>(0);
    }
}