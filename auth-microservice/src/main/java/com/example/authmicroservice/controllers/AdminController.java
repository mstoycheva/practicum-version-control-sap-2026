package com.example.authmicroservice.controllers;

import com.example.authmicroservice.models.User;
import com.example.authmicroservice.services.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RestTemplate restTemplate;

    public AdminController(UserService userService, RestTemplate restTemplate) {
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/";

        try {
            User currentAdmin = userService.findByUsername(principal.getName());
            if (currentAdmin == null || !"admin".equals(currentAdmin.getRole())) return "redirect:/";

            List<User> allUsers = userService.getUsers();
            model.addAttribute("users", allUsers);
            model.addAttribute("totalUsers", allUsers.size());

            List<?> allProjects = List.of();
            List<?> allDocuments = List.of();
            int projectCount;
            int documentCount;

            try {
                allProjects = restTemplate.exchange(
                        "http://localhost:8080/project-microservice/projects/all",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<java.util.Map<String, Object>>>() {}
                ).getBody();

                allDocuments = restTemplate.exchange(
                        "http://localhost:8080/document-microservice/documents/all",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<java.util.Map<String, Object>>>() {}
                ).getBody();

                projectCount = Objects.requireNonNull(restTemplate.exchange(
                        "http://localhost:8080/project-microservice/projects/count",
                        HttpMethod.GET,
                        null,
                        Long.class
                ).getBody()).intValue();

                documentCount = Objects.requireNonNull(restTemplate.exchange(
                        "http://localhost:8080/document-microservice/documents/count",
                        HttpMethod.GET,
                        null,
                        Long.class
                ).getBody()).intValue();

                model.addAttribute("projectsList", allProjects);
                model.addAttribute("documentsList", allDocuments);
                model.addAttribute("projects", projectCount);
                model.addAttribute("documents", documentCount);

            } catch (Exception e) {
                System.err.println("Could not fetch microservice data: " + e.getMessage());
                model.addAttribute("projectsList", List.of());
                model.addAttribute("documentsList", List.of());
                model.addAttribute("projects", 0);
                model.addAttribute("documents", 0);
            }

            model.addAttribute("projectsList", allProjects);
            model.addAttribute("documentsList", allDocuments);
            model.addAttribute("admin", currentAdmin);

            return "adminDashboard";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        try {
            boolean deleted = userService.deleteUser(id);

            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
