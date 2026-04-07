package com.example.documentmicroservice.controllers;

import com.example.documentmicroservice.services.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/versions")
public class VersionController {
    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptVersion(@PathVariable UUID id) {
        versionService.approveVersion(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectVersion(@PathVariable UUID id) {
        versionService.deleteVersion(id);
        return ResponseEntity.ok().build();
    }
}
