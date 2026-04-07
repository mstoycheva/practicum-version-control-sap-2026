package com.example.projectmicroservice.services;

import com.example.projectmicroservice.models.Project;
import com.example.projectmicroservice.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjects(UUID userId) {
        return projectRepository.findByOwnerId(userId);
    }

    public List<Project> getProjects() {
        return projectRepository.findAll();
    }

    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    public long countAllProjects() {
        return projectRepository.count();
    }

    public void saveDocumentInProject(UUID projectId, UUID documentId) {
        Project project = (Project) projectRepository.findByDocId(documentId);
    }
    public List<Project> getProjectsForUser(UUID userId) {
        return projectRepository.findAllVisibleProjects(userId);
    }
}
