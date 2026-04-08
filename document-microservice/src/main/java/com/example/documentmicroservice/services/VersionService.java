package com.example.documentmicroservice.services;
import com.example.documentmicroservice.models.Document;
import com.example.documentmicroservice.models.Version;
import com.example.documentmicroservice.repositories.FileRepository;
import com.example.documentmicroservice.repositories.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class VersionService {

    private final VersionRepository versionRepository;
    private final FileRepository fileRepository;

    public VersionService(VersionRepository versionRepository, FileRepository fileRepository) {
        this.versionRepository = versionRepository;
        this.fileRepository = fileRepository;
    }

    public List<Version> getAllVersions() {
        return (List<Version>) versionRepository.findAll();
    }

    @Transactional
    public Version saveVersion(UUID userId, Document document) {
        Integer lastVersion = versionRepository.findMaxVersionNumberByDocumentId(document.getId());
        int nextVersion = (lastVersion == null) ? 1 : lastVersion + 1;

        Version version = new Version();
        version.setVersionNumber(nextVersion);
        version.setMessage(document.getName());
        version.setDocumentId(document.getId());
        version.setUserId(userId);

        if (nextVersion == 1) {
            version.setActive(true);
            version.setApproved(true);
        } else {
            version.setActive(false);
            version.setApproved(false);
        }

        return versionRepository.save(version);
    }

    public List<Version> findAllByCreatedBy(UUID userId) {
        return versionRepository.findByUserId(userId);
    }

    public Version findById(UUID id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found with id: " + id));
    }

    @Transactional
    public void approveVersion(UUID versionId) {
        Version newVersion = versionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found: " + versionId));

        if (newVersion.isApproved()) {
            return;
        }

        newVersion.setApproved(true);
        newVersion.setActive(true);

        versionRepository.save(newVersion);
    }

    public Version getLastApprovedVersion(UUID documentId) {
        return versionRepository.findFirstByDocumentIdAndActiveTrueOrderByVersionNumberDesc(documentId)
                .orElseThrow(() -> new RuntimeException("No approved versions found for document: " + documentId));
    }

//    @Transactional
//    public void approveVersion(UUID versionId) {
//        Version newVersion = versionRepository.findById(versionId)
//                .orElseThrow(() -> new RuntimeException("Version not found: " + versionId));
//
//        newVersion.setApproved(true);
//        newVersion.setActive(true);
//
//        versionRepository.save(newVersion);
//    }

    @Transactional
    public void deleteVersion(UUID versionId) {
        fileRepository.deleteByVersionId(versionId);
        versionRepository.deleteById(versionId);
    }
}