package com.example.projectmicroservice.repositories;
import com.example.projectmicroservice.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.Document;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p WHERE p.ownerId = :userId OR p.isPublic = true")
    List<Project> findAllVisibleProjects(@Param("userId") UUID userId);
    List<Project> findByOwnerId(UUID ownerId);
    long count();

    default List<Document> findByDocId(UUID docId){
        return findByDocId(docId);
    }
}

