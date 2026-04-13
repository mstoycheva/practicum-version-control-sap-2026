package com.example.authmicroservice.controllers;

import com.example.authmicroservice.models.User;
import com.example.authmicroservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/users/profile-pic/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePic(@PathVariable UUID id) {
        User user = userService.findById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("size of pfp: " + user.getProfilePicture().length + " bytes");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(user.getProfilePicture());
    }
    @GetMapping("/users/profile/{id}")
    public String getUserProfile(@PathVariable UUID id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "profile-page";
    }

    @PostMapping(path = "/users/profile/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @PathVariable UUID id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) throws IOException {

        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            user.setProfilePicture(profilePicture.getBytes());
        }

        userService.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/update-role/{id}")
    public ResponseEntity<?> updateRole(@PathVariable UUID id, @RequestParam String role) {
        try {
            User user = userService.findById(id);
            user.setRole(role.toLowerCase());
            userService.save(user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserRole(@PathVariable UUID id) {
        try {
            User user = userService.findById(id);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<>());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
