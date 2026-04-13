package com.example.documentmicroservice.repositories;
import com.example.documentmicroservice.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCreatedBy(UUID userId);
    List<Document> findByDescription(String description);
    long count();

}
