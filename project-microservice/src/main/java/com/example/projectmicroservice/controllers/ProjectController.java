package com.example.projectmicroservice.controllers;
import com.example.projectmicroservice.models.Project;
import com.example.projectmicroservice.services.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/documents")
    public String newProject(
            @RequestParam("name") String projectName,
            @RequestParam("description") String description,
            @RequestParam("ownerId") UUID ownerId) {
        Project project = new Project();
        project.setName(projectName);
        project.setDescription(description);
        project.setCreatedAt(LocalDateTime.now());

        if (ownerId == null) {
            ownerId = UUID.randomUUID();
        }
        project.setOwnerId(ownerId);

        projectService.saveProject(project);

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + ownerId;
    }

    @PostMapping("/projects/add")
    public String addNewProject(
            @RequestParam("projectName") String name,
            @RequestParam("userId") UUID userId,
            @RequestParam("username") String username,
            @RequestParam(value = "isPublic",defaultValue = "false") boolean isPublic) {

        Project project = new Project();
        project.setName(name);
        project.setOwnerId(userId);
        project.setCreatedAt(LocalDateTime.now());
        project.setDescription("Active");
        project.setPublic(isPublic);

        projectService.saveProject(project);

        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + userId + "&name=" + username;
    }

    @GetMapping("/projects/user/{userId}")
    @ResponseBody
    public List<Project> getProjectsByUser(@PathVariable UUID userId) {
        return projectService.getAllProjects(userId);
    }

    @PostMapping("/projects")
    public String createProject(@ModelAttribute Project project) {
        projectService.saveProject(project);
        return "redirect:http://localhost:8080/document-microservice/documents?userId=" + project.getOwnerId();
    }

    @GetMapping("/projects/count")
    @ResponseBody
    public long countProjects() {
        return projectService.countAllProjects();
    }

    @GetMapping("/projects/all")
    @ResponseBody
    public List<Project> getAllProjects() {
        return projectService.getProjects();
    }

    @GetMapping("/projects/public/{userId}")
    @ResponseBody
    public List<Project> getPublicProjects(@PathVariable UUID userId) {
        return projectService.getProjectsForUser(userId);
    }
}


