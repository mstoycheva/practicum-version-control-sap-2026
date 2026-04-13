package com.example.documentmicroservice.services;
import com.example.documentmicroservice.models.Document;
import com.example.documentmicroservice.repositories.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Map<String, Object> getWorkspaceData(UUID userId) {
        List<Document> allDocs = documentRepository.findByCreatedBy(userId);
        Map<String, Object> data = new HashMap<>();

        Map<String, List<Document>> tree = allDocs.stream()
                .collect(Collectors.groupingBy(doc ->
                        doc.getDescription() != null ? doc.getDescription() : "General"
                ));

        data.put("groupedDocs", tree);

        List<Document> active = allDocs.stream()
                .filter(d -> "Active".equalsIgnoreCase(d.getDescription()))
                .collect(Collectors.toList());
        data.put("activeDocs", active);

        List<Document> drafts = allDocs.stream()
                .filter(d -> "Draft".equalsIgnoreCase(d.getDescription()))
                .collect(Collectors.toList());
        data.put("draftDocs", drafts);

        return data;
    }

    public List<Document> findAllByProjectId(UUID projectId) {
        return documentRepository.findByProjectId(projectId);
    }

    public Document findById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
    }

    public List<Document> findAllByCreatedBy(UUID userId) {
        return documentRepository.findByCreatedBy(userId);
    }

    @Transactional
    public Document saveDocument(MultipartFile file, UUID projectId, String projectName, UUID userId) {
        Document doc = new Document();
        doc.setName(file.getOriginalFilename());
        doc.setProjectId(projectId);
        doc.setDescription(projectName);
        doc.setCreatedBy(userId);
        doc.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    public Optional<Document> getId(UUID docId) {
        return documentRepository.findById(docId);
    }

    public long countAllDocuments() {
        return documentRepository.count();
    }
}
