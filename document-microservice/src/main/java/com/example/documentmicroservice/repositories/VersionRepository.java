package com.example.documentmicroservice.repositories;
import com.example.documentmicroservice.models.Version;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersionRepository extends JpaRepository<Version, UUID> {
    List<Version> findByDocumentId(UUID documentId);
    List<Version> findByUserId(UUID userId);
    Version findVersionById(UUID versionId);
    @Query("SELECT MAX(v.versionNumber) FROM Version v WHERE v.documentId = :documentId")
    Integer findMaxVersionNumberByDocumentId(@Param("documentId") UUID documentId);
    void deleteById(@NonNull UUID id);
    List<Version> findAllByDocumentIdIn(List<UUID> documentIds);
    Optional<Version> findFirstByDocumentIdAndActiveTrueOrderByVersionNumberDesc(UUID documentId);
}
