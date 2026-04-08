package com.example.documentmicroservice.services;
import com.example.documentmicroservice.models.File;
import com.example.documentmicroservice.models.Version;
import com.example.documentmicroservice.repositories.FileRepository;
import com.example.documentmicroservice.repositories.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final VersionRepository versionRepository;
    public FileService(FileRepository fileRepository, VersionRepository versionRepository) {
        this.fileRepository = fileRepository;
        this.versionRepository = versionRepository;
    }
    public List<File> getAllFiles() {return fileRepository.findAll();}

//    public List<File> getAllFilesByUserId(UUID id) {
//        return ;
//    }

    @Transactional
    public File saveFile(UUID versionId, MultipartFile multipartFile) {
        File file = new File();
        file.setVersionId(versionId);
        file.setFile_path(safeGetBytes(multipartFile));
        file.setContent(multipartFile.getContentType());
        file.setHistoryTimestamp(LocalDateTime.now());
        return fileRepository.save(file);
    }
    private byte[] safeGetBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes", e);
        }
    }

    public File getFileEntityByDocumentId(UUID documentId) {
        Version version = versionRepository.findByDocumentId(documentId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Version not found"));
        return fileRepository.findByVersionId(version.getId())
                .orElseThrow(() -> new RuntimeException("File content not found"));
    }

    public File getFileEntityByVersionId(UUID versionId) {
        Version version = versionRepository.findById(versionId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Version not found"));
        return fileRepository.findByVersionId(version.getId())
                .orElseThrow(() -> new RuntimeException("File content not found"));
    }
}
