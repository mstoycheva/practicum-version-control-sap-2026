package com.example.documentmicroservice.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "versions")
public class Version {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="id", updatable=false,nullable=false)
    private UUID id;

    @Column(name = "version_number", nullable =false)
    private int versionNumber;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt=LocalDateTime.now();

//    @JsonProperty("isApproved")
//    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Column(name = "is_approved")
    private boolean approved;

//    @JsonProperty("isActive")
//    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Column(name = "is_active")
    private boolean active;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("isApproved")
    public boolean isApproved() {
        return approved;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return active;
    }

}
