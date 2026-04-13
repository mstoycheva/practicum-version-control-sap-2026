package com.example.documentmicroservice.controllers;

import com.example.documentmicroservice.models.Version;
import com.example.documentmicroservice.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/versions")
public class VersionController {
    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @PatchMapping("/{versionId}/accept")
    public ResponseEntity<String> approveVersion(@PathVariable UUID versionId) {
        try {
            versionService.approveVersion(versionId);
            return ResponseEntity.ok("Version approved successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @GetMapping("/document/{documentId}/current")
    public ResponseEntity<Version> getCurrentVersion(@PathVariable UUID documentId) {
        try {
            Version current = versionService.getLastApprovedVersion(documentId);
            return ResponseEntity.ok(current);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectVersion(@PathVariable UUID id) {
        versionService.deleteVersion(id);
        return ResponseEntity.ok().build();
    }
}
