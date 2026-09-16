package br.edu.ifsp.kaluana.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "responsible_links")
public class ResponsibleLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "responsible_id", nullable = false)
    private UUID responsibleId;

    @Column(name = "primary_user_id", nullable = false)
    private UUID primaryUserId;

    @Column(name="access_level", nullable = false, length = 20)
    private String accessLevel = "DEFAULT";

    @Column(name = "created_at", nullable = false, length = 20)
    private OffsetDateTime createdAt;

    protected ResponsibleLink() {}

    public ResponsibleLink(UUID responsibleId, UUID primaryUserId) {
        this.responsibleId = responsibleId;
        this.primaryUserId = primaryUserId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }
    public UUID getResponsibleId() {
        return responsibleId;
    }
    public UUID getPrimaryUserId() {
        return primaryUserId;
    }
    public String getAccessLevel() {
        return accessLevel;
    }
}
