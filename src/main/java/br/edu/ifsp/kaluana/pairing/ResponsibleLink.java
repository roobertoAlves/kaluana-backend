package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;
import java.util.UUID;

import br.edu.ifsp.kaluana.access.AccessSettings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private LinkRelationship relationship = LinkRelationship.OTHER;

    @Column(name = "share_events", nullable = false)
    private boolean shareEvents;

    @Column(name = "share_check_ins", nullable = false)
    private boolean shareCheckIns;

    @Column(name = "share_usage", nullable = false)
    private boolean shareUsage;

    @Column(name = "share_tasks", nullable = false)
    private boolean shareTasks;

    @Column(name = "share_reports", nullable = false)
    private boolean shareReports;

    protected ResponsibleLink() {}

    public ResponsibleLink(UUID responsibleId, UUID primaryUserId) {
        this.responsibleId = responsibleId;
        this.primaryUserId = primaryUserId;
    }

    public static ResponsibleLink withDefaults(UUID responsibleId, UUID primaryUserId, AccessSettings defaults) {
        ResponsibleLink link = new ResponsibleLink(responsibleId, primaryUserId);
        link.shareEvents = defaults.isShareEvents();
        link.shareCheckIns = defaults.isShareCheckIns();
        link.shareUsage = defaults.isShareUsage();
        link.shareReports = defaults.isShareReports();
        return link;
    }

    public void updatePermissions(LinkPermissionsDTO permissions) {
        this.relationship = permissions.relationship();
        this.shareEvents = permissions.events();
        this.shareCheckIns = permissions.checkins();
        this.shareUsage = permissions.usage();
        this.shareTasks = permissions.tasks();
        this.shareReports = permissions.reports();
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
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public LinkRelationship getRelationship() {
        return relationship;
    }
    public boolean isShareEvents() {
        return shareEvents;
    }
    public boolean isShareCheckIns() {
        return shareCheckIns;
    }
    public boolean isShareUsage() {
        return shareUsage;
    }
    public boolean isShareTasks() {
        return shareTasks;
    }
    public boolean isShareReports() {
        return shareReports;
    }

    public UUID otherSideOf(UUID userId) {
        return userId.equals(primaryUserId) ? responsibleId : primaryUserId;
    }
}
