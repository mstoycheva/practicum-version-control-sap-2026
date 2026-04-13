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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DocumentController {
    private final DocumentService documentService;
    private final FileService fileService;
    private final VersionService versionService;

    public DocumentController(DocumentService documentService, FileService fileService, VersionService versionService) {
        this.documentService = documentService;
        this.fileService = fileService;
        this.versionService = versionService;
    }

    @GetMapping("/documents")
    public String showWorkspace(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "name", required = false, defaultValue = "User") String name,
            Model model) {

        if (userId == null) return "redirect:/error";

        Map<String, Object> workspaceData = documentService.getWorkspaceData(userId);
        @SuppressWarnings("unchecked")
        Map<String, Object> groupedDocs = (Map<String, Object>) workspaceData.getOrDefault("groupedDocs", new HashMap<>());
        List<Document> allDocuments = documentService.findAllByCreatedBy(userId);

        List<Map<String, Object>> allProjects;
        List<UUID> allVisibleDocIds = new ArrayList<>();

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://localhost:8080/project-microservice/projects/public/" + userId;
            allProjects = restTemplate.getForObject(url, List.class);
            model.addAttribute("allProjects", allProjects);

            if (allProjects != null) {
                for (Map<String, Object> proj : allProjects) {
                    String projectName = (String) proj.get("name");

                    List<Document> projectDocs = documentService.findAllByProjectName(projectName);
                    groupedDocs.put(projectName, projectDocs);

                    projectDocs.forEach(d -> allVisibleDocIds.add(d.getId()));
                }
            }
        } catch (Exception e) {
            System.err.println("Project fetch failed: " + e.getMessage());
        }

        List<Version> rawVersions = versionService.findAllByDocumentIds(allVisibleDocIds);

        List<Map<String, Object>> draftDocs = rawVersions.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("message", v.getMessage());
            map.put("versionNumber", v.getVersionNumber());
            map.put("active", v.isActive());
            map.put("approved", v.isApproved());
            if (v.getDocumentId() != null) {
                map.put("documentId", v.getDocumentId());
            }
            return map;
        }).toList();

        String role = "reader";
        try {
            RestTemplate restTemplate = new RestTemplate();
            String authUrl = "http://localhost:8080/auth-microservice/users/" + userId;
            ResponseEntity<Map> response = restTemplate.getForEntity(authUrl, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                role = response.getBody().get("role").toString();
            }
        } catch (Exception e) {
            System.err.println("Auth fetch failed: " + e.getMessage());
        }

        model.addAttribute("userRole", role.toLowerCase());
        model.addAttribute("userId", userId);
        model.addAttribute("username", name);
        model.addAttribute("groupedDocs", groupedDocs);
        model.addAttribute("activeDocs", workspaceData.get("activeDocs"));
        model.addAttribute("draftDocs", draftDocs);
        model.addAttribute("allDocuments", allDocuments);

        return "documents";
    }

    @GetMapping("/documents/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID id) {
        File fileEntity = fileService.getFileEntityByVersionId(id);
        Version version = versionService.findById(id);

        byte[] data = fileEntity.getFile_path();
        String contentType = fileEntity.getContent();
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        String fileName = UriUtils.encode(version.getMessage(), StandardCharsets.UTF_8);

        if (fileName.isEmpty()) {
            fileName = "document_" + id.toString();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping("/documents/user/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable UUID userId) {
        List<Document> userDocs = documentService.findAllByCreatedBy(userId);
        return ResponseEntity.ok(userDocs);
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