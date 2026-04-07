package com.example.documentmicroservice.repositories;
import com.example.documentmicroservice.models.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {
    Optional<File> findByVersionId(UUID versionId);
    void deleteByVersionId(UUID versionId);
}
